package cat.itacademy.s05.t02.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Actions that can be performed on a pet")
public enum PetAction {
    @Schema(description = "Feed: reduces hunger, increases stamina and happiness")
    FEED,

    @Schema(description = "Play: increases happiness, costs stamina, grants XP")
    PLAY,

    @Schema(description = "Train: grants higher XP, increases hunger, costs stamina")
    TRAIN,

    @Schema(description = "Rest: restores stamina, slightly increases hunger")
    REST
}
