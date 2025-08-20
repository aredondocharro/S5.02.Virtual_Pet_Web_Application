package cat.itacademy.s05.t02.service.user;

import cat.itacademy.s05.t02.controller.dto.AuthCreateUserRequest;
import cat.itacademy.s05.t02.controller.dto.AuthLoginRequest;
import cat.itacademy.s05.t02.controller.dto.AuthResponse;
import cat.itacademy.s05.t02.exception.BadRequestException;
import cat.itacademy.s05.t02.exception.ConflictException;
import cat.itacademy.s05.t02.persistence.entity.PermissionEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEnum;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.RoleRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import cat.itacademy.s05.t02.util.JwtUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private UserDetailServiceImpl service;

    private UserEntity user;
    private RoleEntity roleUser;

    @BeforeEach
    void setUp() {
        // ROLE_USER con 2 permisos: READ, CREATE
        PermissionEntity pRead = PermissionEntity.builder().name("READ").build();
        PermissionEntity pCreate = PermissionEntity.builder().name("CREATE").build();
        roleUser = RoleEntity.builder()
                .roleEnum(RoleEnum.USER)
                .permissionList(List.of(pRead, pCreate))
                .build();

        user = UserEntity.builder()
                .id(1L)
                .username("alex")
                .email("alex@example.com")
                .password("$2a$10$hash") // hash ficticio
                .isEnabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .roles(Set.of(roleUser))
                .build();
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    // ---------- loadUserByUsername ----------

    @Test
    @DisplayName("loadUserByUsername: usuario existe → devuelve UserDetails con roles y permisos")
    void loadUserByUsername_ok() {
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));

        var ud = service.loadUserByUsername("  alex@example.com  ");

        assertEquals("alex@example.com", ud.getUsername());
        assertTrue(ud.isEnabled());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ")));
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CREATE")));
        verify(userRepository).findByEmail("alex@example.com");
    }

    @Test
    @DisplayName("loadUserByUsername: usuario no existe → UsernameNotFoundException")
    void loadUserByUsername_notFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }

    // ---------- authenticate + loginUser ----------

    @Test
    @DisplayName("loginUser: email/password en blanco → BadRequestException")
    void login_blank() {
        assertThrows(BadRequestException.class,
                () -> service.loginUser(new AuthLoginRequest(" ", "pwd")));
        assertThrows(BadRequestException.class,
                () -> service.loginUser(new AuthLoginRequest("user@x.com", " ")));
        assertThrows(BadRequestException.class,
                () -> service.loginUser(new AuthLoginRequest(null, "pwd")));
        assertThrows(BadRequestException.class,
                () -> service.loginUser(new AuthLoginRequest("user@x.com", null)));
    }

    @Test
    @DisplayName("loginUser: credenciales incorrectas → BadCredentialsException")
    void login_badCredentials() {
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpwd", user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> service.loginUser(new AuthLoginRequest("alex@example.com", "badpwd")));

        verify(userRepository).findByEmail("alex@example.com");
        verify(passwordEncoder).matches("badpwd", user.getPassword());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("loginUser: OK → genera token y setea SecurityContext")
    void login_ok() {
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", user.getPassword())).thenReturn(true);
        when(jwtUtils.createToken(any())).thenReturn("jwt-token");

        AuthResponse res = service.loginUser(new AuthLoginRequest("  alex@example.com ", "secret123"));

        assertEquals("alex@example.com", res.email());
        assertEquals("jwt-token", res.accessToken());
        assertTrue(res.success());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtils).createToken(any());
    }

    // ---------- createUser (register) ----------

    @Test
    @DisplayName("createUser: validaciones de campos en blanco y password corta")
    void createUser_validations() {
        // username blank
        assertThrows(BadRequestException.class,
                () -> service.createUser(new AuthCreateUserRequest(" ", "a@a.com", "12345678")));
        // email blank
        assertThrows(BadRequestException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", " ", "12345678")));
        // password blank
        assertThrows(BadRequestException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", "a@a.com", " ")));
        // password corta
        assertThrows(BadRequestException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", "a@a.com", "short")));
    }

    @Test
    @DisplayName("createUser: username/email duplicados → ConflictException")
    void createUser_duplicates() {
        when(roleRepository.findByRoleEnum(RoleEnum.USER)).thenReturn(Optional.of(roleUser));

        when(userRepository.existsByUsername("alex")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", "a@a.com", "12345678")));
        verify(userRepository).existsByUsername("alex");

        when(userRepository.existsByUsername("alex")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", "a@a.com", "12345678")));
        verify(userRepository).existsByEmail("a@a.com");
    }

    @Test
    @DisplayName("createUser: ROLE_USER no existe → IllegalStateException")
    void createUser_roleMissing() {
        when(userRepository.existsByUsername("alex")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(roleRepository.findByRoleEnum(RoleEnum.USER)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.createUser(new AuthCreateUserRequest("alex", "a@a.com", "12345678")));
    }

    @Test
    @DisplayName("createUser: OK → guarda usuario, codifica password y emite JWT")
    void createUser_ok() {
        when(userRepository.existsByUsername("alex")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(roleRepository.findByRoleEnum(RoleEnum.USER)).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("12345678")).thenReturn("$encoded");
        // El repositorio devuelve el usuario “creado” con id
        ArgumentCaptor<UserEntity> toSave = ArgumentCaptor.forClass(UserEntity.class);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity e = inv.getArgument(0);
            e.setId(99L);
            return e;
        });
        when(jwtUtils.createToken(any())).thenReturn("new-jwt");

        AuthResponse res = service.createUser(new AuthCreateUserRequest(" alex ", " a@a.com ", "12345678"));

        verify(userRepository).save(toSave.capture());
        UserEntity saved = toSave.getValue();
        assertEquals("alex", saved.getUsername());
        assertEquals("a@a.com", saved.getEmail());
        assertEquals("$encoded", saved.getPassword());
        assertNotNull(saved.getRoles());
        assertTrue(saved.getRoles().stream().anyMatch(r -> r.getRoleEnum() == RoleEnum.USER));

        assertEquals("a@a.com", res.email());
        assertEquals("new-jwt", res.accessToken());
        assertTrue(res.success());
    }
}
