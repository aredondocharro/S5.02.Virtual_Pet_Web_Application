package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PetService {

    private final PetRepository pets;
    private final UserRepository users;

    public PetService(PetRepository pets, UserRepository users) {
        this.pets = pets; this.users = users;
    }

    public List<PetEntity> listMine(String email, boolean isAdmin) {
        return isAdmin ? pets.findAll() : pets.findByOwnerEmail(email);
    }

    public PetEntity create(String email, String name, String color) {
        UserEntity owner = users.findByEmail(email).orElseThrow();
        PetEntity p = PetEntity.builder()
                .name(name).color(color).hunger(50).happiness(50).owner(owner).build();
        return pets.save(p);
    }

    public PetEntity updateMyPet(String email, boolean isAdmin, Long id, int hunger, int happiness) {
        PetEntity p = pets.findById(id).orElseThrow();
        if (!isAdmin && !p.getOwner().getEmail().equals(email)) throw new SecurityException("Forbidden");
        p.setHunger(hunger); p.setHappiness(happiness);
        return pets.save(p);
    }

    public void deleteMyPet(String email, boolean isAdmin, Long id) {
        if (!isAdmin && !pets.existsByIdAndOwnerEmail(id, email)) throw new SecurityException("Forbidden");
        pets.deleteById(id);
    }
}
