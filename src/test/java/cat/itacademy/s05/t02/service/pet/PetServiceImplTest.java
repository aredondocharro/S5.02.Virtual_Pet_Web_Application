package cat.itacademy.s05.t02.service.pet;

import cat.itacademy.s05.t02.controller.dto.ActionResultResponse;
import cat.itacademy.s05.t02.controller.dto.PetActionRequest;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetAction;
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
                .color("pink")
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
    @DisplayName("listMine: USER devuelve solo sus mascotas; ADMIN devuelve todas")
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
    @DisplayName("create: ok con defaults; valida nombre/color")
    void create_ok_and_validations() {
        when(userRepository.findByEmail("u@x.com")).thenReturn(Optional.of(user));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> {
            PetEntity p = inv.getArgument(0);
            p.setId(123L);
            return p;
        });

        var created = service.create("u@x.com", "Axo", "pink");

        assertEquals(123L, created.getId());
        assertEquals(30, created.getHunger());
        assertEquals(70, created.getStamina());
        assertEquals(60, created.getHappiness());
        assertEquals(1, created.getLevel());
        assertEquals(0, created.getXpInLevel());
        assertEquals(EvolutionStage.BABY, created.getStage());

        // validations
        assertThrows(BadRequestException.class, () -> service.create("u@x.com", "   ", "pink"));
        assertThrows(BadRequestException.class, () -> service.create("u@x.com", "Axo", "   "));
    }

    @Test
    @DisplayName("create: lanza NotFound si el usuario no existe")
    void create_user_not_found() {
        when(userRepository.findByEmail("no@x.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create("no@x.com", "Axo", "pink"));
        verify(userRepository).findByEmail("no@x.com");
        verifyNoInteractions(petRepository);
    }

    // ========== updateMyPet ==========
    @Test
    @DisplayName("updateMyPet: forbidden si no es owner y no es admin")
    void update_forbidden() {
        PetEntity otherOwnersPet = PetEntity.builder()
                .id(11L)
                .owner(UserEntity.builder().email("other@x.com").build())
                .hunger(10).happiness(10)
                .build();
        when(petRepository.findById(11L)).thenReturn(Optional.of(otherOwnersPet));

        assertThrows(ForbiddenException.class, () ->
                service.updateMyPet("u@x.com", false, 11L, 20, 20));
    }

    @Test
    @DisplayName("updateMyPet (ADMIN): actualiza correctamente")
    void update_admin_ok() {
        PetEntity stored = PetEntity.builder()
                .id(10L).owner(user).hunger(10).happiness(10).build();
        when(petRepository.findById(10L)).thenReturn(Optional.of(stored));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetEntity updated = service.updateMyPet("whoever@x.com", true, 10L, 20, 30);

        assertEquals(20, updated.getHunger());
        assertEquals(30, updated.getHappiness());
        verify(petRepository).findById(10L);
        verify(petRepository).save(stored);
    }

    @Test
    @DisplayName("updateMyPet: lanza NotFound si la mascota no existe")
    void update_not_found() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.updateMyPet("u@x.com", true, 999L, 10, 10));
        verify(petRepository).findById(999L);
        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMyPet: validación de rango 0..100")
    void update_range_validation() {
        // No hay stubs aquí para evitar UnnecessaryStubbing: el test falla antes de tocar el repo
        assertThrows(BadRequestException.class, () ->
                service.updateMyPet("u@x.com", true, 10L, -1, 50));   // hunger inválido
        assertThrows(BadRequestException.class, () ->
                service.updateMyPet("u@x.com", true, 10L, 10, 200));  // happiness inválido
    }

    // ========== deleteMyPet ==========
    @Test
    @DisplayName("deleteMyPet (ADMIN): elimina OK")
    void delete_admin_ok() {
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        doNothing().when(petRepository).delete(pet);

        service.deleteMyPet("admin@x.com", true, 10L);

        verify(petRepository).findById(10L);
        verify(petRepository).delete(pet);
    }

    @Test
    @DisplayName("deleteMyPet: forbidden para no-owner no-admin")
    void delete_forbidden() {
        PetEntity other = PetEntity.builder()
                .id(11L)
                .owner(UserEntity.builder().email("other@x.com").build())
                .build();
        when(petRepository.findById(11L)).thenReturn(Optional.of(other));

        assertThrows(ForbiddenException.class,
                () -> service.deleteMyPet("u@x.com", false, 11L));
        verify(petRepository).findById(11L);
        verify(petRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteMyPet: NotFound si no existe")
    void delete_not_found() {
        when(petRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.deleteMyPet("u@x.com", true, 404L));
        verify(petRepository).findById(404L);
        verify(petRepository, never()).delete(any());
    }

    // ========== applyAction ==========
    @Test
    @DisplayName("applyAction (USER): usa findByIdAndOwnerEmail y persiste cambios")
    void applyAction_user() {
        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com")).thenReturn(Optional.of(pet));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest();
        req.setAction(PetAction.PLAY);

        ActionResultResponse res = service.applyAction(10L, "u@x.com", req, false);

        assertNotNull(res.getPet());
        assertEquals("Axo", res.getPet().getName());
        verify(petRepository).findByIdAndOwnerEmail(10L, "u@x.com");
        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    @DisplayName("applyAction (ADMIN): usa findById")
    void applyAction_admin() {
        when(petRepository.findById(10L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(PetEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest();
        req.setAction(PetAction.FEED);

        ActionResultResponse res = service.applyAction(10L, "admin@x.com", req, true);

        assertNotNull(res.getPet());
        verify(petRepository).findById(10L);
        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    @DisplayName("applyAction: action requerido")
    void applyAction_requires_action() {
        assertThrows(BadRequestException.class, () ->
                service.applyAction(10L, "u@x.com", new PetActionRequest(), false));
        assertThrows(BadRequestException.class, () ->
                service.applyAction(10L, "u@x.com", null, false));
    }

    @Test
    @DisplayName("applyAction: not found según rol")
    void applyAction_not_found_cases() {
        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com")).thenReturn(Optional.empty());
        PetActionRequest req = new PetActionRequest();
        req.setAction(PetAction.PLAY);
        assertThrows(NotFoundException.class, () ->
                service.applyAction(10L, "u@x.com", req, false));

        when(petRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                service.applyAction(10L, "admin@x.com", req, true));
    }

    @Test
    @DisplayName("applyAction: sube de nivel y llama a recalcStage() cuando cambia el nivel (USER)")
    void applyAction_triggers_recalc_stage_on_levelup() {
        // Spy para comprobar recalcStage()
        PetEntity spyPet = spy(pet);
        spyPet.setXpInLevel(95); // a 5 del siguiente nivel

        when(petRepository.findByIdAndOwnerEmail(10L, "u@x.com"))
                .thenReturn(Optional.of(spyPet));
        when(petRepository.save(any(PetEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PetActionRequest req = new PetActionRequest();
        req.setAction(PetAction.PLAY); // +10 XP => level up

        ActionResultResponse res = service.applyAction(10L, "u@x.com", req, false);

        assertNotNull(res.getPet());
        assertEquals(2, spyPet.getLevel());
        assertEquals(5, spyPet.getXpInLevel());
        verify(spyPet, times(1)).recalcStage(); // invocado al cambiar el nivel
        verify(petRepository).save(spyPet);
    }
}

