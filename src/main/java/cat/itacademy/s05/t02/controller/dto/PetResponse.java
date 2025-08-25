package cat.itacademy.s05.t02.controller.dto;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.domain.PetColor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pet data returned by the API")
public record PetResponse(

        @Schema(description = "Pet ID", example = "42")
        Long id,

        @Schema(description = "Display name of the pet", example = "Axo")
        String name,

        @Schema(description = "Main color of the pet", example = "pink")
        PetColor color,

        @Schema(description = "Hunger 0..100 (0=satiated, 100=very hungry)", example = "40")
        int hunger,

        @Schema(description = "Stamina 0..100 (0=exhausted, 100=full energy)", example = "55")
        int stamina,

        @Schema(description = "Happiness 0..100", example = "70")
        int happiness,

        @Schema(description = "Current level 1..15", example = "3")
        int level,

        @Schema(description = "XP in the current level 0..100", example = "25")
        int xpInLevel,

        @Schema(description = "Evolution stage derived from level", example = "BABY")
        EvolutionStage stage,

        @Schema(description = "Owner email", example = "alex@example.com")
        String ownerEmail
) {}


