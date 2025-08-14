package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Payload para actualizar el estado de la mascota")
public class PetUpdateRequest {

    @Schema(description = "Nivel de hambre (0-100)", example = "30")
    @Min(0) @Max(100)
    private int hunger;

    @Schema(description = "Nivel de felicidad (0-100)", example = "80")
    @Min(0) @Max(100)
    private int happiness;

    public PetUpdateRequest() {}

    public PetUpdateRequest(int hunger, int happiness) {
        this.hunger = hunger;
        this.happiness = happiness;
    }

    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public void setHunger(int hunger) { this.hunger = hunger; }
    public void setHappiness(int happiness) { this.happiness = happiness; }
}
