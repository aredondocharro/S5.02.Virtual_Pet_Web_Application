package cat.itacademy.s05.t02.config;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.PetServiceImpl;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = PetServiceCacheTest.TestBootConfig.class)
@ActiveProfiles("test")
class PetServiceCacheTest {

    // ====== Mocks de repos ======
    @MockBean PetRepository petRepository;
    @MockBean UserRepository userRepository;

    // @MockBean cat.itacademy.s05.t02.controller.PetController petController;

    // ====== Beans reales (inyectados desde nuestra mini-app de test) ======
    @Autowired
    @Qualifier("testCacheManager")
    CacheManager cacheManager;

    @Autowired
    PetServiceImpl service;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache("petsByOwner").clear();
        reset(petRepository, userRepository);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
            // añade más excludes si tu proyecto auto-configura otras cosas que no necesitas en este test
    })
    static class TestBootConfig {
        // Anidamos una TestConfiguration para separar claramente los beans de caché/servicio
        @TestConfiguration
        @EnableCaching
        static class TestConfig {

            @Bean("testCaffeine")
            @Primary
            Caffeine<Object, Object> testCaffeine() {
                return Caffeine.newBuilder()
                        .expireAfterWrite(60, TimeUnit.SECONDS)
                        .maximumSize(1_000);
            }

            @Bean("testCacheManager")
            @Primary
            CacheManager testCacheManager(@Qualifier("testCaffeine") Caffeine<Object, Object> caffeine) {
                CaffeineCacheManager mgr = new CaffeineCacheManager("petsByOwner");
                mgr.setCaffeine(caffeine);
                return mgr;
            }

            @Bean
            @Primary
            PetServiceImpl petService(PetRepository pets, UserRepository users) {
                return new PetServiceImpl(pets, users);
            }
        }
    }

    // ===== Helpers =====
    private UserEntity owner(String email) {
        UserEntity u = new UserEntity();
        u.setEmail(email);
        return u;
    }

    private PetEntity pet(Long id, String name, UserEntity owner) {
        PetEntity p = new PetEntity();
        p.setId(id);
        p.setName(name);
        p.setColor("blue");
        p.setHunger(30);
        p.setStamina(70);
        p.setHappiness(60);
        p.setLevel(1);
        p.setXpInLevel(0);
        p.setStage(EvolutionStage.BABY);
        p.setOwner(owner);
        return p;
    }

    // ===== Tests =====

    @Test
    @DisplayName("listMine cachea por email y evita segundo hit al repo")
    void listMine_caches_by_email() {
        String email = "user@example.com";
        UserEntity u = owner(email);
        PetEntity p1 = pet(1L, "Axolotl", u);

        when(petRepository.findByOwnerEmail(email)).thenReturn(List.of(p1));

        service.listMine(email, false); // 1ª: DB
        service.listMine(email, false); // 2ª: cache

        verify(petRepository, times(1)).findByOwnerEmail(email);
        verifyNoMoreInteractions(petRepository);
    }

    @Test
    @DisplayName("create invalida cache del owner y ADMIN_ALL")
    void create_evicts_owner_and_admin_cache() {
        String email = "user@example.com";
        UserEntity u = owner(email);
        PetEntity saved = pet(10L, "NewPet", u);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(petRepository.save(any(PetEntity.class))).thenReturn(saved);

        when(petRepository.findByOwnerEmail(email)).thenReturn(List.of());
        service.listMine(email, false); // seed
        verify(petRepository, times(1)).findByOwnerEmail(email);

        service.create(email, "NewPet", "red"); // evict

        when(petRepository.findByOwnerEmail(email)).thenReturn(List.of(saved));
        service.listMine(email, false);

        verify(petRepository, times(2)).findByOwnerEmail(email);
    }

    @Test
    @DisplayName("updateMyPet invalida y fuerza nueva consulta")
    void update_evicts_and_reload() {
        String email = "user@example.com";
        UserEntity u = owner(email);
        PetEntity existing = pet(5L, "Buddy", u);

        when(petRepository.findByOwnerEmail(email)).thenReturn(List.of(existing));
        service.listMine(email, false);
        verify(petRepository, times(1)).findByOwnerEmail(email);

        when(petRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(petRepository.save(existing)).thenReturn(existing);
        service.updateMyPet(email, false, 5L, 40, 80); // evict

        service.listMine(email, false); // DB again
        verify(petRepository, times(2)).findByOwnerEmail(email);
    }

    @Test
    @DisplayName("deleteMyPet invalida y fuerza recarga")
    void delete_evicts_and_reload() {
        String email = "user@example.com";
        UserEntity u = owner(email);
        PetEntity existing = pet(7L, "Neo", u);

        when(petRepository.findByOwnerEmail(email)).thenReturn(List.of(existing));
        service.listMine(email, false);
        verify(petRepository, times(1)).findByOwnerEmail(email);

        when(petRepository.findById(7L)).thenReturn(Optional.of(existing));
        doNothing().when(petRepository).delete(existing);
        service.deleteMyPet(email, false, 7L); // evict

        service.listMine(email, false); // DB again
        verify(petRepository, times(2)).findByOwnerEmail(email);

        when(petRepository.findById(999L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class,
                () -> service.deleteMyPet(email, false, 999L));
    }
}


