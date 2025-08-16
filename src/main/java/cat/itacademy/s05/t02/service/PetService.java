package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class PetService {

    private final PetRepository pets;
    private final UserRepository users;

    public PetService(PetRepository pets, UserRepository users) {
        this.pets = pets; this.users = users;
    }

    public List<PetEntity> listMine(String email, boolean isAdmin) {
        log.debug("Listing pets for user='{}' (admin={})", email, isAdmin);
        List<PetEntity> result = isAdmin ? pets.findAll() : pets.findByOwnerEmail(email);
        log.info("Found {} pets for user='{}' (admin={})", result.size(), email, isAdmin);
        return result;
    }

    public PetEntity create(String email, String name, String color) {
        log.debug("Creating pet for owner='{}' name='{}' color='{}'", email, name, color);
        UserEntity owner = users.findByEmail(email).orElseThrow();
        PetEntity p = PetEntity.builder()
                .name(name).color(color).hunger(50).happiness(50).owner(owner).build();
        PetEntity saved = pets.save(p);
        log.info("Pet created id={} name='{}' owner='{}'", saved.getId(), name, email);
        return saved;
    }

    public PetEntity updateMyPet(String email, boolean isAdmin, Long id, int hunger, int happiness) {
        log.debug("Updating pet id={} by '{}' (admin={}) hunger={} happiness={}",
                id, email, isAdmin, hunger, happiness);
        PetEntity p = pets.findById(id).orElseThrow();
        if (!isAdmin && !p.getOwner().getEmail().equals(email)) {
            log.warn("Update forbidden: user='{}' is not owner of pet id={}", email, id);
            throw new SecurityException("Forbidden");
        }
        p.setHunger(hunger);
        p.setHappiness(happiness);
        PetEntity updated = pets.save(p);
        log.info("Pet id={} updated by '{}'", id, email);
        return updated;
    }

    public void deleteMyPet(String email, boolean isAdmin, Long id) {
        log.warn("Delete requested for pet id={} by '{}' (admin={})", id, email, isAdmin);
        if (!isAdmin && !pets.existsByIdAndOwnerEmail(id, email)) {
            log.warn("Delete forbidden: user='{}' tried to delete non-owned pet id={}", email, id);
            throw new SecurityException("Forbidden");
        }
        pets.deleteById(id);
        log.info("Pet id={} deleted by '{}'", id, email);
    }
}

