package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.AuthCreateUserRequest;
import cat.itacademy.s05.t02.controller.dto.AuthLoginRequest;
import cat.itacademy.s05.t02.controller.dto.AuthResponse;
import cat.itacademy.s05.t02.service.UserDetailServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Register and login (JWT)")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailServiceImpl userDetailService;

    @Operation(
            summary = "Register a new user with email and password (USER role by default)"
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateUserRequest body) {
        log.info("Register request received for email='{}'", body.email());
        try {
            AuthResponse res = userDetailService.createUser(body);
            log.info("User registered successfully: email='{}'", body.email());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            log.warn("Register failed for email='{}': {}", body.email(), e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Login by email and password",
            description = "Returns a valid JWT 'Authorization: Bearer <token>'"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest body) {
        log.info("Login attempt for email='{}'", body.email());
        try {
            AuthResponse res = userDetailService.loginUser(body);
            log.info("Login successful for email='{}'", body.email());
            log.debug("JWT issued for '{}'", body.email());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.warn("Login failed for email='{}': {}", body.email(), e.getMessage());
            throw e;
        }
    }
}


