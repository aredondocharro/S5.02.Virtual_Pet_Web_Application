package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.ActionResultResponse;
import cat.itacademy.s05.t02.controller.dto.PetActionRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse; // <-- NEW
import cat.itacademy.s05.t02.controller.mapper.PetMapper;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetColor;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ForbiddenException;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.engine.PetRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
@Slf4j
public class PetServiceImpl {

    private final PetRepository pets;
    private final UserRepository users;

    public PetServiceImpl(PetRepository pets, UserRepository users) {
        this.pets = pets;
        this.users = users;
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "petsByOwner",
            key = "#isAdmin ? 'ADMIN_ALL' : #email"
    )
    public List<PetEntity> listMine(String email, boolean isAdmin) {
        log.debug("Listing pets for user='{}' (admin={})", email, isAdmin);
        List<PetEntity> result = isAdmin ? pets.findAll() : pets.findByOwnerEmail(email);
        log.info("Found {} pets for user='{}' (admin={})", result.size(), email, isAdmin);
        return result;
    }

    @Caching(evict = {
            @CacheEvict(value = "petsByOwner", key = "#email"),
            @CacheEvict(value = "petsByOwner", key = "'ADMIN_ALL'")
    })
    public PetEntity create(String email, String name, PetColor color) {
        log.debug("Creating pet for owner='{}' name='{}' color='{}'", email, name, color);

        UserEntity owner = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (!StringUtils.hasText(name)) throw new BadRequestException("Pet name must not be blank");
        if (color == null) throw new BadRequestException("Pet color must not be blank");

        PetEntity p = PetEntity.builder()
                .name(name.trim())
                .color(color)
                .hunger(30)
                .stamina(70)
                .happiness(60)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .owner(owner)
                .build();

        PetEntity saved = pets.save(p);
        log.info("Pet created id={} name='{}' owner='{}'", saved.getId(), saved.getName(), email);
        return saved;
    }

    @Caching(evict = {
            @CacheEvict(value = "petsByOwner", key = "#isAdmin ? 'ADMIN_ALL' : #email"),
            @CacheEvict(value = "petsByOwner", key = "'ADMIN_ALL'")
    })
    public PetEntity updateMyPet(String email, boolean isAdmin, Long id, int hunger, int happiness) {
        log.debug("Updating pet id={} by '{}' (admin={}) hunger={} happiness={}",
                id, email, isAdmin, hunger, happiness);

        if (hunger < 0 || hunger > 100) throw new BadRequestException("Hunger must be between 0 and 100");
        if (happiness < 0 || happiness > 100) throw new BadRequestException("Happiness must be between 0 and 100");

        PetEntity p = pets.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet with id=" + id + " not found"));

        if (!isAdmin && !p.getOwner().getEmail().equals(email)) {
            log.warn("Update forbidden: user='{}' is not owner of pet id={}", email, id);
            throw new ForbiddenException("You do not own this pet");
        }

        p.setHunger(hunger);
        p.setHappiness(happiness);
        PetEntity updated = pets.save(p);
        log.info("Pet id={} updated by '{}'", id, email);
        return updated;
    }

    @Caching(evict = {
            @CacheEvict(value = "petsByOwner", key = "#isAdmin ? 'ADMIN_ALL' : #email"),
            @CacheEvict(value = "petsByOwner", key = "'ADMIN_ALL'")
    })
    public void deleteMyPet(String email, boolean isAdmin, Long id) {
        log.warn("Delete requested for pet id={} by '{}' (admin={})", id, email, isAdmin);

        PetEntity p = pets.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet with id=" + id + " not found"));

        if (!isAdmin && !p.getOwner().getEmail().equals(email)) {
            log.warn("Delete forbidden: user='{}' tried to delete non-owned pet id={}", email, id);
            throw new ForbiddenException("You do not own this pet");
        }

        pets.delete(p);
        log.info("Pet id={} deleted by '{}'", id, email);
    }

    @Caching(evict = {
            @CacheEvict(value = "petsByOwner", key = "#isAdmin ? 'ADMIN_ALL' : #email"),
            @CacheEvict(value = "petsByOwner", key = "'ADMIN_ALL'")
    })
    public ActionResultResponse applyAction(Long petId, String email, PetActionRequest request, boolean isAdmin) {
        if (request == null || request.action() == null) {
            throw new BadRequestException("Action is required");
        }

        PetEntity pet = isAdmin
                ? pets.findById(petId).orElseThrow(() -> new NotFoundException("Pet not found"))
                : pets.findByIdAndOwnerEmail(petId, email)
                .orElseThrow(() -> new NotFoundException("Pet not found or not owned by user"));

        log.debug("Applying action {} to pet id={} by '{}' (admin={})",
                request.action(), petId, email, isAdmin);

        // Precondition check (exhaustion, hunger/energy gates)
        String deny = PetRules.precondition(pet, request.action());
        if (deny != null) throw new BadRequestException(deny);

        var result = PetRules.apply(pet, request.action());
        pets.save(pet);

        ActionResultResponse res = new ActionResultResponse(
                PetMapper.toResponse(pet),
                result.message(),
                result.xpGained()
        );

        log.info("Action {} applied to pet id={} by '{}' (admin={}), xpGained={}, level={}, stage={}",
                request.action(), petId, email, isAdmin, result.xpGained(), pet.getLevel(), pet.getStage());

        return res;
    }

    @Transactional(readOnly = true)
    public PetResponse getMyPetDtoById(String email, boolean isAdmin, Long id) {
        PetEntity pet = pets.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet not found: " + id));

        if (!isAdmin && !pet.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You cannot access this pet");
        }
        return PetMapper.toResponse(pet);
    }

    @Transactional(readOnly = true)
    public PetEntity getMyPetById(String email, boolean isAdmin, Long id) {
        PetEntity pet = pets.findById(id)
                .orElseThrow(() -> new NotFoundException("Pet not found: " + id));

        if (!isAdmin && !pet.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("You cannot access this pet");
        }
        return pet;
    }
}








