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
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;

    // === 1) Cargar por EMAIL ===
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorityList = buildAuthorities(userEntity);
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

        // Permissions (si tu RoleEntity tiene permissionList)
        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissionList().stream())
                .forEach(permission -> list.add(new SimpleGrantedAuthority(permission.getName())));

        return list;
    }

    // === 2) LOGIN por EMAIL ===
    public AuthResponse loginUser(AuthLoginRequest req) {
        String email = req.email();
        String password = req.password();

        Authentication authentication = this.authenticate(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.createToken(authentication);

        return new AuthResponse(email, "User logged in successfully", accessToken, true);
    }

    public Authentication authenticate(String email, String rawPassword) {
        UserDetails userDetails = loadUserByUsername(email);
        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
    }

    // === 3) REGISTER por EMAIL + ROLE_USER auto ===
    public AuthResponse createUser(AuthCreateUserRequest req) {
        String email = req.email();
        String password = req.password();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        RoleEntity roleUser = roleRepository.findByRoleEnum(RoleEnum.USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleUser);

        UserEntity newUser = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .isEnabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .roles(roles)
                .build();

        UserEntity userCreated = userRepository.save(newUser);

        List<SimpleGrantedAuthority> authorityList = buildAuthorities(userCreated);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userCreated.getEmail(),
                userCreated.getPassword(),
                authorityList
        );

        String accessToken = jwtUtils.createToken(authentication);

        return new AuthResponse(
                userCreated.getEmail(),
                "User created successfully",
                accessToken,
                true
        );
    }
}

