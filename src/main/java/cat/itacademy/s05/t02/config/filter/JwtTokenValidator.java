package cat.itacademy.s05.t02.config.filter;

import cat.itacademy.s05.t02.util.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;



public class JwtTokenValidator extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring("Bearer ".length()).trim();
            try {
                DecodedJWT decoded = jwtUtils.validateToken(jwtToken);
                String username = jwtUtils.extractUsername(decoded);

                var claim = decoded.getClaim("authorities");
                java.util.List<String> list = claim != null && !claim.isNull()
                        ? claim.asList(String.class)  // ← esperamos ARRAY
                        : java.util.List.of();

                // (opcional) fallback CSV para tokens antiguos
                if (list == null) {
                    String csv = claim.asString();
                    list = (csv == null || csv.isBlank())
                            ? java.util.List.of()
                            : java.util.Arrays.stream(csv.split(","))
                            .map(String::trim).filter(s -> !s.isBlank()).toList();
                    // log.warn("JWT authorities en CSV: considera regenerar el token como array.");
                }

                var authorities = list.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList();

                var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (JWTVerificationException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid JWT: " + ex.getMessage() + "\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

