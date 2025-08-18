package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User profile")
@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class UserController {

    private final UserProfileService profile;

    public UserController(UserProfileService profile) {
        this.profile = profile;
    }

    @Operation(summary = "My profile (me)", description = "Return authenticated user profile")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication auth) {
        String email = auth.getName();
        log.debug("Profile requested by user='{}'", email);

        UserProfileResponse res = profile.getMe(email);
        log.info("Profile successfully retrieved for '{}'", email);

        return ResponseEntity.ok(res);
    }

    @Operation(summary = "Update my profile (me)")
    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileResponse> updateMe(
            Authentication auth,
            @RequestBody @Valid UserProfileUpdateRequest req) {

        String email = auth.getName();
        log.info("Profile update requested by user='{}' with new username='{}'", email, req.getUsername());

        UserProfileResponse updated = profile.updateMe(email, req);
        log.debug("Profile updated successfully for '{}'", email);

        return ResponseEntity.ok(updated);
    }
}

