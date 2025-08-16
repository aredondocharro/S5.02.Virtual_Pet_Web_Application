package cat.itacademy.s05.t02.service;

import cat.itacademy.s05.t02.controller.dto.AuthCreateUserRequest;
import cat.itacademy.s05.t02.controller.dto.AuthLoginRequest;
import cat.itacademy.s05.t02.controller.dto.AuthResponse;
import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEnum;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import cat.itacademy.s05.t02.persistence.repository.RoleRepository;
import cat.itacademy.s05.t02.persistence.repository.UserRepository;
import cat.itacademy.s05.t02.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;

    // === 1) Cargar por EMAIL ===
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email='{}'", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email='{}'", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        List<SimpleGrantedAuthority> authorityList = buildAuthorities(userEntity);
        log.info("User '{}' loaded with {} authorities", email, authorityList.size());

        return new User(
                userEntity.getEmail(),                    // principal = email
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNonExpired(),
                userEntity.isCredentialsNonExpired(),
                userEntity.isAccountNonLocked(),
                authorityList
        );
    }

    private List<SimpleGrantedAuthority> buildAuthorities(UserEntity userEntity) {
        List<SimpleGrantedAuthority> list = new ArrayList<>();

        // Roles -> ROLE_*
        userEntity.getRoles()
                .forEach(role -> list.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().name())));

        // Permissions
        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissionList().stream())
                .forEach(permission -> list.add(new SimpleGrantedAuthority(permission.getName())));

        return list;
    }

    // === 2) LOGIN por EMAIL ===
    public AuthResponse loginUser(AuthLoginRequest req) {
        String email = req.email();
        String password = req.password();

        log.info("Login attempt for email='{}'", email);
        Authentication authentication = this.authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication);
        log.info("Login successful for email='{}'", email);

        return new AuthResponse(email, "User logged in successfully", accessToken, true);
    }

    public Authentication authenticate(String email, String rawPassword) {
        UserDetails userDetails = loadUserByUsername(email);
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            log.warn("Authentication failed for email='{}': bad credentials", email);
            throw new BadCredentialsException("Invalid email or password");
        }
        log.debug("Authentication OK for '{}'", email);
        return new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
    }

    // === 3) REGISTER ===
    public AuthResponse createUser(AuthCreateUserRequest req) {
        String username = req.username();
        String email = req.email();
        String password = req.password();

        log.info("Register attempt: username='{}', email='{}'", username, email);

        if (userRepository.existsByUsername(username)) {
            log.warn("Register failed: username '{}' already in use", username);
            throw new IllegalArgumentException("Username already in use");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Register failed: email '{}' already in use", email);
            throw new IllegalArgumentException("Email already in use");
        }

        RoleEntity roleUser = roleRepository.findByRoleEnum(RoleEnum.USER)
                .orElseThrow(() -> {
                    log.error("ROLE_USER not found in DB");
                    return new IllegalStateException("ROLE_USER not found");
                });

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleUser);

        UserEntity newUser = UserEntity.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .isEnabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .roles(roles)
                .build();

        UserEntity userCreated = userRepository.save(newUser);
        log.info("User '{}' registered successfully with id={}", email, userCreated.getId());

        List<SimpleGrantedAuthority> authorityList = buildAuthorities(userCreated);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userCreated.getEmail(),
                userCreated.getPassword(),
                authorityList
        );

        String accessToken = jwtUtils.createToken(authentication);
        log.debug("JWT issued for newly created user '{}'", email);

        return new AuthResponse(
                userCreated.getEmail(),
                "User created successfully",
                accessToken,
                true
        );
    }
}


