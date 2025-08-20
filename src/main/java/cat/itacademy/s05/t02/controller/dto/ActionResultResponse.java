package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after applying an action to a pet")
public record ActionResultResponse(
        @Schema(description = "Updated pet after applying the action")
        PetResponse pet,

        @Schema(description = "Summary message of the action effects")
        String message,

        @Schema(description = "XP gained from the action", example = "10")
        int xpGained
) {}
