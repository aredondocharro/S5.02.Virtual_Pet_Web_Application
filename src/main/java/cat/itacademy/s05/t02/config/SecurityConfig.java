package cat.itacademy.s05.t02.config;

import cat.itacademy.s05.t02.config.filter.JwtTokenValidator;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import cat.itacademy.s05.t02.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtUtils jwtUtils;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring HTTP security: stateless + JWT filter");
        SecurityFilterChain chain = http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // público
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/error").permitAll()

                        // /app/*
                        .requestMatchers(HttpMethod.GET, "/app/get").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/app/post").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/app/put").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/app/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/app/patch").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            log.warn("Unauthorized access to '{} {}' -> 401", req.getMethod(), req.getRequestURI());
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            log.warn("Access denied to '{} {}' -> 403", req.getMethod(), req.getRequestURI());
                            res.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                )

                // Coloca el validador JWT antes del UsernamePasswordAuthenticationFilter
                .addFilterBefore(new JwtTokenValidator(jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .build();

        log.info("Security filter chain built. JWT validator registered before UsernamePasswordAuthenticationFilter");
        return chain;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        AuthenticationManager manager = cfg.getAuthenticationManager();
        log.debug("AuthenticationManager bean created");
        return manager;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        log.debug("DaoAuthenticationProvider configured with custom UserDetailsService and PasswordEncoder");
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("PasswordEncoder bean (BCrypt) created");
        return new BCryptPasswordEncoder();
    }
}



