package cat.itacademy.s05.t02.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evolution stage of the pet")
public enum EvolutionStage {
    @Schema(description = "Levels 1–5: tadpole-like baby")
    BABY,

    @Schema(description = "Levels 6–10: teen, growing limbs")
    TEEN,

    @Schema(description = "Levels 11–15: fully-grown adult")
    ADULT;

    public static EvolutionStage fromLevel(int level) {
        if (level <= 5) return BABY;
        if (level <= 10) return TEEN;
        return ADULT;
    }
}
