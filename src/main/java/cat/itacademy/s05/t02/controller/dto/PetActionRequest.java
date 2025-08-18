package cat.itacademy.s05.t02.controller.dto;

import cat.itacademy.s05.t02.domain.PetAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to apply an action to a pet")
public class PetActionRequest {

    @Schema(
            description = "Action to apply (FEED, PLAY, TRAIN, REST)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "PLAY"
    )
    @NotNull
    private PetAction action;

    @Schema(
            description = "Optional intensity/amount, reserved for future use",
            example = "1"
    )
    private Integer amount;

    public PetAction getAction() { return action; }
    public void setAction(PetAction action) { this.action = action; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}

