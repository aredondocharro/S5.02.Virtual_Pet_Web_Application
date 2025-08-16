package cat.itacademy.s05.t02.config.handler;

import cat.itacademy.s05.t02.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         org.springframework.security.core.AuthenticationException ex) throws IOException {
        log.warn("401 Unauthorized on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ErrorResponse.of(401, "UNAUTHORIZED", "Authentication required", req.getRequestURI());
        mapper.writeValue(res.getOutputStream(), body);
    }
}