package cat.itacademy.s05.t02.controller.dto;

import cat.itacademy.s05.t02.domain.PetAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to apply an action to a pet")
public record PetActionRequest(

        @Schema(
                description = "Action to apply (FEED, PLAY, TRAIN, REST)",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "PLAY"
        )
        @NotNull
        PetAction action,

        @Schema(
                description = "Optional intensity/amount, reserved for future use",
                example = "1"
        )
        Integer amount
) {}


