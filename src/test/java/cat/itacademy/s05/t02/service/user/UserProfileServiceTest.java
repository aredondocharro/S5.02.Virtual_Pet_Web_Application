package cat.itacademy.s05.t02.service.user;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.controller.mapper.UserMapper;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ConflictException;
import cat.itacademy.s05.t02.exception.NotFoundException;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository users;

    @InjectMocks
    private UserProfileService service;

    private UserEntity makeUser() {
        return UserEntity.builder()
                .id(1L)
                .username("alex")
                .email("alex@example.com")
                .bio("Hi!")
                .avatarUrl("http://img")
                .isEnabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
    }

    // -------- getMe --------

    @Test
    @DisplayName("getMe: OK → devuelve profile")
    void getMe_ok() {
        var u = makeUser();
        when(users.findByEmail("alex@example.com")).thenReturn(Optional.of(u));

        UserProfileResponse res = service.getMe("alex@example.com");

        assertEquals("alex", res.username());
        assertEquals("alex@example.com", res.email());
        verify(users).findByEmail("alex@example.com");
    }

    @Test
    @DisplayName("getMe: not found → NotFoundException")
    void getMe_notFound() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getMe("missing@example.com"));
    }

    // -------- updateMe --------

    @Test
    @DisplayName("updateMe: req null → BadRequestException")
    void update_nullReq() {
        assertThrows(BadRequestException.class, () -> service.updateMe("alex@example.com", null));
    }

    @Test
    @DisplayName("updateMe: usuario no existe → NotFoundException")
    void update_notFound() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.updateMe("missing@example.com", new UserProfileUpdateRequest(null, null, null)));
    }

    @Test
    @DisplayName("updateMe: username en blanco → BadRequestException")
    void update_blankUsername() {
        var u = makeUser();
        when(users.findByEmail("alex@example.com")).thenReturn(Optional.of(u));

        var req = new UserProfileUpdateRequest("   ", null, null); // explícitamente en blanco

        assertThrows(BadRequestException.class, () -> service.updateMe("alex@example.com", req));
    }

    @Test
    @DisplayName("updateMe: username duplicado → ConflictException")
    void update_duplicateUsername() {
        var u = makeUser();
        when(users.findByEmail("alex@example.com")).thenReturn(Optional.of(u));
        when(users.existsByUsername("newname")).thenReturn(true);

        var req = new UserProfileUpdateRequest("newname", null, null);

        assertThrows(ConflictException.class, () -> service.updateMe("alex@example.com", req));
    }

    @Test
    @DisplayName("updateMe: avatarUrl en blanco → BadRequestException")
    void update_blankAvatar() {
        var u = makeUser();
        when(users.findByEmail("alex@example.com")).thenReturn(Optional.of(u));

        var req = new UserProfileUpdateRequest(null, null, "   ");

        assertThrows(BadRequestException.class, () -> service.updateMe("alex@example.com", req));
    }

    @Test
    @DisplayName("updateMe: OK → actualiza username, bio y avatar (trim)")
    void update_ok() {
        var u = makeUser();
        when(users.findByEmail("alex@example.com")).thenReturn(Optional.of(u));
        when(users.existsByUsername("alex-new")).thenReturn(false);

        var req = new UserProfileUpdateRequest("  alex-new  ", "  New bio  ", "  http://new-img  ");

        UserProfileResponse res = service.updateMe("alex@example.com", req);

        assertEquals("alex-new", res.username());
        assertEquals("http://new-img", res.avatarUrl());
        assertEquals("New bio", res.bio());

        // entidad mutada
        assertEquals("alex-new", u.getUsername());
        assertEquals("http://new-img", u.getAvatarUrl());
        assertEquals("New bio", u.getBio());

        verify(users).findByEmail("alex@example.com");
    }
}
