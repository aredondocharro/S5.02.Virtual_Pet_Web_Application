package cat.itacademy.s05.t02.service.pet;

import cat.itacademy.s05.t02.controller.dto.ActionResultResponse;
import cat.itacademy.s05.t02.controller.dto.PetActionRequest;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetAction;
import cat.itacademy.s05.t02.domain.PetColor;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ForbiddenException;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.PetRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.PetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock private PetRepository petRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private PetServiceImpl service;

    private UserEntity user;
    private PetEntity pet;

    @BeforeEach
    void init() {
        user = UserEntity.builder().id(1L).email("u@x.com").build();
        pet = PetEntity.builder()
                .id(10L)
                .name("Axo")
                .color(PetColor.PINK) // 👈 enum ahora
                .owner(user)
                .hunger(50)
                .stamina(50)
                .happiness(50)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .build();
    }

    // ========== listMine ==========
    @Test
    @DisplayName("listMine: USER returns only own pets; ADMIN returns all")
    void listMine_user_vs_admin() {
        when(petRepository.findByOwnerEmail("u@x.com")).thenReturn(List.of(pet));
        when(petRepository.findAll()).thenReturn(List.of(pet, pet));

        var userList = service.listMine("u@x.com", false);
        var adminList = service.listMine("admin@x.com", true);

        assertEquals(1, userList.size());
        assertEquals(2, adminList.size());
    }

    // ========== create ==========
    @Test
    @DisplayName("create: ok with defaults; validates name and non-null color")
    void create_ok_and_validations() {
        when(userRepository.findByEmail("u@x.com")).thenReturn(Optional.of(user));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> {
            PetEntity p = inv.getArgument(0);
            p.setId(123L);
            return p;
        });

        // create OK
        var created = service.create("u@x.com", "Axo", PetColor.PINK); // 👈 enum
        assertEquals(123L, created.getId());
        assertEquals(30, created.getHunger());
        assertEquals(70, created.getStamina());
        assertEquals(60, created.getHappiness());
        assertEquals(1, created.getLevel());
        assertEquals(0, created.getXpInLevel());
        assertEquals(EvolutionStage.BABY, created.getStage());

        // validations
        assertThrows(BadRequestException.class, () -> service.create("u@x.com", "   ", PetColor.PINK));
        assertThrows(BadRequestException.class, () -> service.create("u@x.com", "Axo", null)); // 👈 color null
    }

    @Test
    @DisplayName("create: throws NotFound when user does not exist")
    void create_user_not_found() {
        when(userRepository.findByEmail("no@x.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create("no@x.com", "Axo", PetColor.PINK));
        verify(userRepository).findByEmail("no@x.com");
        verifyNoInteractions(petRepository);
    }

    // ========== updateMyPet ==========
    @Test
    @DisplayName("updateMyPet: forbidden if not owner and not admin")
    void update_forbidden() {
        PetEntity otherOwnersPet = PetEntity.builder()
                .id(11L)
                .owner(UserEntity.builder().email("other@x.com").build())
                .color(PetColor.BLACK) // 👈 evita null
                .hunger(10).happiness(10)
                .build();
        when(petRepository.findById(11L)).thenReturn(Optional.of(otherOwnersPet));

        assertThrows(ForbiddenException.class, () ->
                service.updateMyPet("u@x.com", false, 11L, 20, 20));
    }

    @Test
    @DisplayName("updateMyPet (ADMIN): updates correctly")
    void update_admin_ok() {
        PetEntity stored = PetEntity.builder()
                .id(10L).owner(user).color(PetColor.WHITE)
                .hunger(10).happiness(10).build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(stored));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetEntity updated = service.updateMyPet("whoever@x.com", true, 10L, 20, 30);

        assertEquals(20, updated.getHunger());
        assertEquals(30, updated.getHappiness());
        verify(petRepository).findById(10L);
        verify(petRepository).save(stored);
    }

    @Test
    @DisplayName("updateMyPet: throws NotFound when pet does not exist")
    void update_not_found() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.updateMyPet("u@x.com", true, 999L, 10, 10));
        verify(petRepository).findById(999L);
        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMyPet: range validation 0..100")
    void update_range_validation() {
        // No stubs: el método falla antes de tocar el repo
        assertThrows(BadRequestException.class, () ->
                service.updateMyPet("u@x.com", true, 10L, -1, 50));   // invalid hunger
        assertThrows(BadRequestException.class, () ->
                service.updateMyPet("u@x.com", true, 10L, 10, 200));  // invalid happiness
    }

    // ========== deleteMyPet ==========
    @Test
    @DisplayName("deleteMyPet (ADMIN): deletes OK")
    void delete_admin_ok() {
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        doNothing().when(petRepository).delete(pet);

        service.deleteMyPet("admin@x.com", true, 10L);

        verify(petRepository).findById(10L);
        verify(petRepository).delete(pet);
    }

    @Test
    @DisplayName("deleteMyPet: forbidden for non-owner non-admin")
    void delete_forbidden() {
        PetEntity other = PetEntity.builder()
                .id(11L)
                .owner(UserEntity.builder().email("other@x.com").build())
                .color(PetColor.ORANGE)
                .build();
        when(petRepository.findById(11L)).thenReturn(Optional.of(other));

        assertThrows(ForbiddenException.class,
                () -> service.deleteMyPet("u@x.com", false, 11L));
        verify(petRepository).findById(11L);
        verify(petRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteMyPet: NotFound when pet does not exist")
    void delete_not_found() {
        when(petRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.deleteMyPet("u@x.com", true, 404L));
        verify(petRepository).findById(404L);
        verify(petRepository, never()).delete(any());
    }

    // ========== applyAction ==========
    @Test
    @DisplayName("applyAction (USER): uses findByIdAndOwnerEmail and persists changes")
    void applyAction_user() {
        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com")).thenReturn(Optional.of(pet));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest(PetAction.PLAY, null);

        ActionResultResponse res = service.applyAction(10L, "u@x.com", req, false);

        assertNotNull(res.pet());
        assertEquals("Axo", res.pet().name());
        verify(petRepository).findByIdAndOwnerEmail(10L, "u@x.com");
        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    @DisplayName("applyAction (ADMIN): uses findById")
    void applyAction_admin() {
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest(PetAction.FEED, null);

        ActionResultResponse res = service.applyAction(10L, "admin@x.com", req, true);

        assertNotNull(res.pet());
        verify(petRepository).findById(10L);
        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    @DisplayName("applyAction: action required")
    void applyAction_requires_action() {
        assertThrows(BadRequestException.class, () ->
                service.applyAction(10L, "u@x.com", new PetActionRequest(null, null), false));
        assertThrows(BadRequestException.class, () ->
                service.applyAction(10L, "u@x.com", null, false));
    }

    @Test
    @DisplayName("applyAction: not found depending on role")
    void applyAction_not_found_cases() {
        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com")).thenReturn(Optional.empty());
        PetActionRequest req = new PetActionRequest(PetAction.PLAY, null);
        assertThrows(NotFoundException.class, () ->
                service.applyAction(10L, "u@x.com", req, false));

        when(petRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                service.applyAction(10L, "admin@x.com", req, true));
    }

    @Test
    @DisplayName("applyAction: level-up triggers recalcStage() when level changes (USER)")
    void applyAction_triggers_recalc_stage_on_levelup() {
        // Spy to verify recalcStage()
        PetEntity spyPet = spy(pet);
        spyPet.setXpInLevel(95); // 5 to next level

        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com"))
                .thenReturn(Optional.of(spyPet));
        when(petRepository.save(any(PetEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest(PetAction.PLAY, null); // +10 XP => level up

        ActionResultResponse res = service.applyAction(10L, "u@x.com", req, false);

        assertNotNull(res.pet());
        assertEquals(2, spyPet.getLevel());
        assertEquals(5, spyPet.getXpInLevel());
        verify(spyPet, times(1)).recalcStage(); // called when level changes
        verify(petRepository).save(spyPet);
    }
}



