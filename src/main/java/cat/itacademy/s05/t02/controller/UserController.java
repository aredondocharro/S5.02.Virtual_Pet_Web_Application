package cat.itacademy.s05.t02.controller;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.controller.dto.UserProfileUpdateRequest;
import cat.itacademy.s05.t02.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User profile")
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserProfileService profile;

    public UserController(UserProfileService profile) {
        this.profile = profile;
    }

    @Operation(summary = "My profile (me)",
            description = "Return user profile authenticated")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(profile.getMe(email));
    }

    @Operation(
            summary = "Actualizate my profile (me)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileUpdateRequest.class),
                            examples = @ExampleObject(name = "updateProfile",
                                    value = """
                            {
                              "username": "AlexGamer",
                              "bio": "Cat lover. Pixel artist.",
                              "avatarUrl": "https://i.pravatar.cc/200?img=12"
                            }
                            """)))
    )
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            Authentication auth,
            @RequestBody @Valid UserProfileUpdateRequest req) {
        String email = auth.getName();
        return ResponseEntity.ok(profile.updateMe(email, req));
    }
}
