package cat.itacademy.s05.t02.config;

import cat.itacademy.s05.t02.config.filter.JwtTokenValidator;
import cat.itacademy.s05.t02.util.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenValidatorTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid Bearer token → sets Authentication with authorities (array claim)")
    void setsAuth_whenValidToken_withArrayAuthorities() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/pets");
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer VALID");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        DecodedJWT decoded = mock(DecodedJWT.class);
        when(jwtUtils.validateToken("VALID")).thenReturn(decoded);
        when(jwtUtils.extractUsername(decoded)).thenReturn("alex@example.com");
        when(decoded.getId()).thenReturn("jti-123");
        when(decoded.getClaim("authorities")).thenReturn(FakeClaim.ofList(List.of("ROLE_USER", "READ")));

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("alex@example.com", auth.getName());
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        Set<String> auths = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        assertTrue(auths.contains("ROLE_USER"));
        assertTrue(auths.contains("READ"));

        verify(chain, times(1)).doFilter(req, res);
    }

    @Test
    @DisplayName("Valid Bearer token → supports CSV authorities and de-duplicates")
    void supportsCsvAuthorities_andDedup() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer VALID");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        DecodedJWT decoded = mock(DecodedJWT.class);
        when(jwtUtils.validateToken("VALID")).thenReturn(decoded);
        when(jwtUtils.extractUsername(decoded)).thenReturn("alex@example.com");
        when(decoded.getId()).thenReturn("jti-xyz");
        when(decoded.getClaim("authorities")).thenReturn(FakeClaim.ofCsv("ROLE_USER, READ, , ROLE_USER  ,  CREATE"));

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        var set = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of("ROLE_USER", "READ", "CREATE"), set);

        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Valid token with no authorities claim → sets auth with empty authorities")
    void validToken_noAuthorities() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer VALID");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        DecodedJWT decoded = mock(DecodedJWT.class);
        when(jwtUtils.validateToken("VALID")).thenReturn(decoded);
        when(jwtUtils.extractUsername(decoded)).thenReturn("user@x.com");
        when(decoded.getId()).thenReturn("jti-noauth");
        // missing claim -> null (tests the 'claim == null' branch)
        when(decoded.getClaim("authorities")).thenReturn(null);

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().isEmpty());
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Skips when Authorization header is missing")
    void skip_whenNoHeader() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest(); // no header
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("Skips when Authorization header is not Bearer")
    void skip_whenNotBearer() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("Header 'Bearer' (or with just spaces) → treated as non-Bearer and the string is followed")
    void bearer_only_is_treated_as_non_bearer() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer   ");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // no exception expected
        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(req, res);
        verifyNoInteractions(jwtUtils);
    }


    @Test
    @DisplayName("Invalid token (validateToken throws) → throws AuthenticationException and does NOT call chain")
    void invalidToken_throwsAuthException() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer BAD");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtils.validateToken("BAD")).thenThrow(new JWTVerificationException("Invalid"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> filter.doFilter(req, res, chain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, never()).doFilter(any(), any());
    }


    @Test
    @DisplayName("Token missing subject (sub) → throws AuthenticationException")
    void missingSubject_throws() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer VALID");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        DecodedJWT decoded = mock(DecodedJWT.class);
        when(jwtUtils.validateToken("VALID")).thenReturn(decoded);
        when(jwtUtils.extractUsername(decoded)).thenReturn("   "); // blank
        when(decoded.getClaim("authorities")).thenReturn(FakeClaim.nullClaim());

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class,
                () -> filter.doFilter(req, res, chain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain, never()).doFilter(any(), any());
    }


    @Test
    @DisplayName("Does not overwrite context if already authenticated")
    void doesNotOverwrite_existingAuthentication() throws Exception {
        var existing = new UsernamePasswordAuthenticationToken("pre@x.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        JwtUtils jwtUtils = mock(JwtUtils.class);
        JwtTokenValidator filter = new JwtTokenValidator(jwtUtils);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(HttpHeaders.AUTHORIZATION, "Bearer VALID");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        DecodedJWT decoded = mock(DecodedJWT.class);
        when(jwtUtils.validateToken("VALID")).thenReturn(decoded);
        when(jwtUtils.extractUsername(decoded)).thenReturn("alex@example.com");
        when(decoded.getClaim("authorities")).thenReturn(FakeClaim.ofList(List.of("ROLE_USER")));

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertSame(existing, auth, "Should keep the existing authentication");
        verify(chain).doFilter(req, res);
    }

    static final class FakeClaim implements Claim {
        private final boolean isNull;
        private final List<String> list;
        private final String str;

        private FakeClaim(boolean isNull, List<String> list, String str) {
            this.isNull = isNull;
            this.list = list;
            this.str = str;
        }

        static Claim ofList(List<String> values) { return new FakeClaim(false, values, null); }
        static Claim ofCsv(String csv)          { return new FakeClaim(false, null, csv); }
        static Claim nullClaim()                { return new FakeClaim(true, null, null); }

        @Override public boolean isNull() { return isNull; }
        @Override public boolean isMissing() { return false; } // <-- NEW for java-jwt 4.5.x
        @Override public String asString() { return str; }
        @Override @SuppressWarnings("unchecked")
        public <T> List<T> asList(Class<T> tClazz) { return (List<T>) list; }

        // Unused in these tests
        @Override public Boolean asBoolean() { return null; }
        @Override public Integer asInt() { return null; }
        @Override public Long asLong() { return null; }
        @Override public Double asDouble() { return null; }
        @Override public java.util.Date asDate() { return null; }
        @Override public <T> T[] asArray(Class<T> tClazz) { return null; }
        @Override public java.util.Map<String, Object> asMap() { return null; }
        @Override public <T> T as(Class<T> tClazz) { return null; }
    }
}


