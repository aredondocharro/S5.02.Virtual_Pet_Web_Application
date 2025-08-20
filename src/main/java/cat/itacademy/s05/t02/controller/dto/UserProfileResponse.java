package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@Schema(description = "Authenticated user profile response")
@JsonInclude(JsonInclude.Include.NON_NULL) // opcional: evita nulls en JSON
public record UserProfileResponse(

        @Schema(example = "7")
        Long id,

        @Schema(example = "alex@example.com")
        String email,

        @Schema(description = "Visible name", example = "Alex")
        String username,

        @Schema(description = "URL profile picture", example = "https://i.pravatar.cc/200?img=5")
        String avatarUrl,

        @Schema(description = "Short bio", example = "Cat lover. Pixel artist.")
        String bio,

        @Schema(description = "User role(s)", example = "[\"USER\",\"ADMIN\"]")
        List<String> roles
) {}

