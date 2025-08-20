package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload to update a pet's state")
public record PetUpdateRequest(

        @Schema(description = "Hunger level (0-100)", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Min(0) @Max(100)
        Integer hunger,

        @Schema(description = "Happiness level (0-100)", example = "80", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Min(0) @Max(100)
        Integer happiness
) {}

