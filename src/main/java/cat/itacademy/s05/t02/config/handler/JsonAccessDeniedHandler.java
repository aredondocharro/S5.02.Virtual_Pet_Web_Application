package cat.itacademy.s05.t02.config.handler;

import cat.itacademy.s05.t02.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       org.springframework.security.access.AccessDeniedException ex) throws IOException {
        log.warn("403 Forbidden on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = ErrorResponse.of(403, "FORBIDDEN",
                "You do not have permission to perform this action", req.getRequestURI());
        mapper.writeValue(res.getOutputStream(), body);
    }
}
