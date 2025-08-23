package cat.itacademy.s05.t02.config;

import cat.itacademy.s05.t02.config.filter.JwtTokenValidator;
import cat.itacademy.s05.t02.config.handler.JsonAccessDeniedHandler;
import cat.itacademy.s05.t02.config.handler.JsonAuthenticationEntryPoint;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import cat.itacademy.s05.t02.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
// ✅ Posicionaremos el JWT validator DESPUÉS de ExceptionTranslationFilter
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            // ✅ inyectamos el ObjectMapper ya configurado por Spring (con JavaTimeModule)
            ObjectMapper objectMapper
    ) throws Exception {

        log.info("Configuring HTTP security: stateless + JWT filter");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // ✅ usamos el mapper del contexto (evita el error con Instant)
                        .authenticationEntryPoint(new JsonAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new JsonAccessDeniedHandler(objectMapper))
                )

                .authenticationProvider(authenticationProvider)

                // ✅ Colocamos el validador DESPUÉS del ExceptionTranslationFilter,
                // quedará entre ExceptionTranslationFilter y AuthorizationFilter.
                .addFilterAfter(new JwtTokenValidator(jwtUtils), ExceptionTranslationFilter.class);

        log.info("Security filter chain built. JWT validator registered after ExceptionTranslationFilter");
        return http.build();
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}






