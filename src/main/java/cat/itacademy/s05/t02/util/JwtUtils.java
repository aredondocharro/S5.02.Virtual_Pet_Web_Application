package cat.itacademy.s05.t02.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.user.generator}")
    private String issuer;

    @Value("${security.jwt.exp-minutes:30}")
    private long expMinutes;

    public String createToken(Authentication authentication){
        Algorithm algorithm = Algorithm.HMAC256(this.secret);

        String subject = authentication.getName(); // email
        String[] authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // ej: ROLE_USER, READ
                .toArray(String[]::new);

        long now = System.currentTimeMillis();
        Date iat = new Date(now);
        Date nbf = new Date(now);
        Date exp = new Date(now + expMinutes * 60_000);

        String token = JWT.create()
                .withIssuer(this.issuer)
                .withSubject(subject)
                .withArrayClaim("authorities", authorities)   // ARRAY
                .withIssuedAt(iat)
                .withNotBefore(nbf)
                .withExpiresAt(exp)
                .withJWTId(java.util.UUID.randomUUID().toString())
                .sign(algorithm);

        log.debug("JWT created for sub='{}' exp='{}' authorities={}", subject, exp, authorities.length);
        return token;
    }

    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .acceptLeeway(3) // segundos de margen
                    .build();

            DecodedJWT decoded = verifier.verify(token);
            log.info("JWT verified: sub='{}' jti='{}' exp='{}'", decoded.getSubject(), decoded.getId(), decoded.getExpiresAt());
            return decoded;
        } catch (JWTVerificationException e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            throw new JWTVerificationException("Token is not valid: " + e.getMessage(), e);
        }
    }

    public String extractUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject();
    }

    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }

    public Map<String, Claim> retrieveAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }
}


