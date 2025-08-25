package cat.itacademy.s05.t02.persistence.repository;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetColor;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // use embedded H2
@TestPropertySource(properties = {
        // H2 in PostgreSQL compatibility mode to reduce dialect surprises
        "spring.datasource.url=jdbc:h2:mem:petdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.show-sql=false"
})
class PetRepositoryTest {

    @Autowired private PetRepository petRepository;
    @Autowired private UserRepository userRepository;

    private UserEntity newUser(String email) {
        return UserEntity.builder()
                .email(email)
                .password("x")
                .build();
    }

    private PetEntity newPet(String name, PetColor color, UserEntity owner) {
        return PetEntity.builder()
                .name(name)
                .color(color)
                .hunger(30)
                .stamina(70)
                .happiness(60)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .owner(owner)
                .build();
    }

    @Test
    @DisplayName("findByIdAndOwnerEmail: returns pet when id and owner email match")
    void findByIdAndOwnerEmail_ok() {
        UserEntity u = userRepository.save(newUser("a@b.com"));
        PetEntity p = petRepository.save(newPet("Axo", PetColor.BLACK, u));

        Optional<PetEntity> found = petRepository.findByIdAndOwnerEmail(p.getId(), "a@b.com");

        assertTrue(found.isPresent());
        assertEquals("Axo", found.get().getName());
        assertEquals("a@b.com", found.get().getOwner().getEmail());
    }

    @Test
    @DisplayName("findByIdAndOwnerEmail: empty when id belongs to a different owner")
    void findByIdAndOwnerEmail_wrong_owner() {
        UserEntity u1 = userRepository.save(newUser("a@b.com"));
        UserEntity u2 = userRepository.save(newUser("c@d.com"));
        PetEntity p = petRepository.save(newPet("Blob", PetColor.BLACK, u1));

        Optional<PetEntity> found = petRepository.findByIdAndOwnerEmail(p.getId(), "c@d.com");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findByIdAndOwnerEmail: empty when id does not exist")
    void findByIdAndOwnerEmail_not_found() {
        Optional<PetEntity> found = petRepository.findByIdAndOwnerEmail(999L, "nobody@x.com");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findByOwnerEmail: returns only pets belonging to the given email")
    void findByOwnerEmail_counts() {
        UserEntity u1 = userRepository.save(newUser("a@b.com"));
        UserEntity u2 = userRepository.save(newUser("c@d.com"));

        petRepository.save(newPet("Axo", PetColor.PINK, u1));
        petRepository.save(newPet("Blob", PetColor.BLACK, u1));
        petRepository.save(newPet("Zig", PetColor.ORANGE, u2));

        List<PetEntity> petsU1 = petRepository.findByOwnerEmail("a@b.com");
        List<PetEntity> petsU2 = petRepository.findByOwnerEmail("c@d.com");
        List<PetEntity> petsNone = petRepository.findByOwnerEmail("none@x.com");

        assertEquals(2, petsU1.size());
        assertEquals(1, petsU2.size());
        assertTrue(petsNone.isEmpty());
    }

    @Test
    @DisplayName("Saving a pet assigns an ID (identity/sequence generated)")
    void id_is_generated_on_save() {
        UserEntity u = userRepository.save(newUser("a@b.com"));
        PetEntity saved = petRepository.save(newPet("Axo", PetColor.PINK, u));

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }
}

