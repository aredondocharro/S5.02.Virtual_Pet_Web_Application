package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.AuthCreateUserRequest;
import cat.itacademy.s05.t02.controller.dto.AuthLoginRequest;
import cat.itacademy.s05.t02.controller.dto.AuthResponse;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Register and login (JWT)")
@Slf4j
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailServiceImpl userDetailService;

    @Operation(summary = "Register a new user with email and password (USER role by default)")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateUserRequest body) {
        log.info("Register request received for email='{}'", body.email());
        AuthResponse res = userDetailService.createUser(body);
        log.info("User registered successfully: email='{}'", body.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Login by email and password", description = "Returns a valid JWT 'Authorization: Bearer <token>'")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest body) {
        log.info("Login attempt for email='{}'", body.email());
        AuthResponse res = userDetailService.loginUser(body);
        log.info("Login successful for email='{}'", body.email());
        log.debug("JWT issued for '{}'", body.email());
        return ResponseEntity.ok(res);
    }
}



