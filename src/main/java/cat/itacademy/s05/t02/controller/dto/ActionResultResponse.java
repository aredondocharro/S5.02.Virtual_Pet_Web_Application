package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after applying an action to a pet")
public class ActionResultResponse {

    @Schema(description = "Updated pet after applying the action")
    private PetResponse pet;

    @Schema(description = "Summary message of the action effects")
    private String message;

    @Schema(description = "XP gained from the action", example = "10")
    private int xpGained;

    public PetResponse getPet() { return pet; }
    public void setPet(PetResponse pet) { this.pet = pet; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getXpGained() { return xpGained; }
    public void setXpGained(int xpGained) { this.xpGained = xpGained; }
}
