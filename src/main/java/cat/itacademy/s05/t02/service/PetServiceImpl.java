package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.ActionResultResponse;
import cat.itacademy.s05.t02.controller.dto.PetActionRequest;
import cat.itacademy.s05.t02.controller.mapper.PetMapper;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ForbiddenException;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.engine.PetRules;
import lombok.extern.slf4j.Slf4j;
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
    public List<PetEntity> listMine(String email, boolean isAdmin) {
        log.debug("Listing pets for user='{}' (admin={})", email, isAdmin);
        List<PetEntity> result = isAdmin ? pets.findAll() : pets.findByOwnerEmail(email);
        log.info("Found {} pets for user='{}' (admin={})", result.size(), email, isAdmin);
        return result;
    }

    /** Create with new default stats (game-ready). */
    public PetEntity create(String email, String name, String color) {
        log.debug("Creating pet for owner='{}' name='{}' color='{}'", email, name, color);

        UserEntity owner = users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        if (!StringUtils.hasText(name)) throw new BadRequestException("Pet name must not be blank");
        if (!StringUtils.hasText(color)) throw new BadRequestException("Pet color must not be blank");

        PetEntity p = PetEntity.builder()
                .name(name.trim())
                .color(color.trim())
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

    public ActionResultResponse applyAction(Long petId, String email, PetActionRequest request, boolean isAdmin) {
        if (request == null || request.getAction() == null) {
            throw new BadRequestException("Action is required");
        }

        // Owner-scope lookup when not admin
        PetEntity pet = isAdmin
                ? pets.findById(petId).orElseThrow(() -> new NotFoundException("Pet not found"))
                : pets.findByIdAndOwnerEmail(petId, email)
                .orElseThrow(() -> new NotFoundException("Pet not found or not owned by user"));

        log.debug("Applying action {} to pet id={} by '{}' (admin={})",
                request.getAction(), petId, email, isAdmin);

        var result = PetRules.apply(pet, request.getAction());
        pets.save(pet);

        var res = new ActionResultResponse();
        res.setPet(PetMapper.toResponse(pet));
        res.setXpGained(result.xpGained);
        res.setMessage(result.message);

        log.info("Action {} applied to pet id={} by '{}' (admin={}), xpGained={}, level={}, stage={}",
                request.getAction(), petId, email, isAdmin, result.xpGained, pet.getLevel(), pet.getStage());
        return res;
    }
}




