package cat.itacademy.s05.t02.controller.dto;

import cat.itacademy.s05.t02.domain.PetColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to create a pet")
public record PetCreateRequest(

        @Schema(description = "Visible name", example = "Neko", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(
                description = "Main color",
                example = "pink",
                allowableValues = {"pink","black","white","orange"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        PetColor color
) {}
