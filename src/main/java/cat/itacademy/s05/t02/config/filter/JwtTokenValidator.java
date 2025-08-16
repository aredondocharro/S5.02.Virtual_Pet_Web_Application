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

@Slf4j
public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String path = request.getRequestURI();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token for path='{}'", path);
            chain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring("Bearer ".length()).trim();
        try {

            DecodedJWT decoded = jwtUtils.validateToken(jwtToken);
            String username = jwtUtils.extractUsername(decoded);
            String jti = decoded.getId();

            var claim = decoded.getClaim("authorities");
            java.util.List<String> list = claim != null && !claim.isNull()
                    ? claim.asList(String.class)  // ← esperamos ARRAY
                    : java.util.List.of();


            if (list == null) {
                String csv = claim.asString();
                list = (csv == null || csv.isBlank())
                        ? java.util.List.of()
                        : java.util.Arrays.stream(csv.split(","))
                        .map(String::trim).filter(s -> !s.isBlank()).toList();
                log.warn("JWT authorities provided as CSV for sub='{}' (consider issuing array-style claim).", username);
            }

            var authorities = list.stream()
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .toList();

            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            log.info("JWT valid: sub='{}' jti='{}' authorities={} path='{}'",
                    username, jti, authorities.size(), path);

        } catch (JWTVerificationException ex) {
            log.warn("Invalid JWT on path='{}': {}", path, ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid JWT: " + ex.getMessage() + "\"}");
            return;
        } catch (Exception ex) {

            log.error("Error processing JWT on path='{}': {}", path, ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid JWT\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}


