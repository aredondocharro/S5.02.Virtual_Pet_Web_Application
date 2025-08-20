package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;


@Schema(description = "Editable profile fields")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileUpdateRequest(

        @Schema(description = "New visible name (optional)", example = "AlexGamer")
        // @Size(min = 3, max = 40, message = "Username length must be 3..40")
        String username,

        @Schema(description = "New bio (optional)", example = "Cat lover. Pixel artist.")
        // @Size(max = 160, message = "Bio must be at most 160 characters")
        String bio,

        @Schema(description = "New avatar URL (optional)", example = "https://i.pravatar.cc/200?img=12")
        // @URL(message = "avatarUrl must be a valid URL")
        String avatarUrl
) {}

