package cat.itacademy.s05.t02.config.filter;

import cat.itacademy.s05.t02.util.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
public class JwtTokenValidator extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        final String path = request.getRequestURI();

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            log.debug("No Authorization header for path='{}'", path);
            chain.doFilter(request, response);
            return;
        }
        String header = authHeader.trim();
        if (header.length() < BEARER.length() || !header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            log.debug("Authorization header present but not Bearer for path='{}'", path);
            chain.doFilter(request, response);
            return;
        }
        String jwtToken = header.substring(BEARER.length()).trim();
        if (jwtToken.isEmpty()) {
            log.warn("Empty Bearer token on path='{}'", path);
            throw new JWTVerificationException("Empty Bearer token");
        }

        try {
            DecodedJWT decoded = jwtUtils.validateToken(jwtToken);

            String username = jwtUtils.extractUsername(decoded);
            if (username == null || username.isBlank()) {
                throw new JWTVerificationException("Token missing subject (sub)");
            }

            String jti = decoded.getId();

            // Authorities: soporta array y CSV legacy
            var claim = decoded.getClaim("authorities");
            List<String> rawAuthorities;
            if (claim == null || claim.isNull()) {
                rawAuthorities = Collections.emptyList();
            } else {
                List<String> list = claim.asList(String.class);
                if (list != null) {
                    rawAuthorities = list;
                } else {
                    String csv = claim.asString();
                    rawAuthorities = (csv == null || csv.isBlank())
                            ? Collections.emptyList()
                            : Arrays.stream(csv.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                    log.warn("JWT authorities provided as CSV for sub='{}' (consider issuing array-style claim).", username);
                }
            }
            var uniqueAuthorities = new LinkedHashSet<String>(rawAuthorities);
            var authorities = uniqueAuthorities.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .toList();

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } else {
                log.debug("Authentication already present in context for path='{}', skipping set.", path);
            }

            log.info("JWT valid: sub='{}' jti='{}' authorities={} path='{}'",
                    username, jti, authorities.size(), path);

            chain.doFilter(request, response);

        } catch (JWTVerificationException ex) {
            log.warn("Invalid JWT on path='{}': {}", path, ex.getMessage());
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.warn("JWT parsing error on path='{}': {}", path, ex.getMessage());
            throw new JWTVerificationException("Invalid JWT token");
        }
    }
}




