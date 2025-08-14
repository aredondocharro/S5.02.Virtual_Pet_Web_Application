package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de una mascota")
public class PetResponse {

    @Schema(description = "ID de la mascota", example = "42")
    private Long id;

    @Schema(description = "Nombre visible de la mascota", example = "Neko")
    private String name;

    @Schema(description = "Color principal", example = "blue")
    private String color;

    @Schema(description = "Nivel de hambre (0-100)", example = "50")
    private int hunger;

    @Schema(description = "Nivel de felicidad (0-100)", example = "65")
    private int happiness;

    @Schema(description = "Email del propietario (solo informativo)", example = "user@example.com")
    private String ownerEmail;

    public PetResponse() {}

    public PetResponse(Long id, String name, String color, int hunger, int happiness, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.hunger = hunger;
        this.happiness = happiness;
        this.ownerEmail = ownerEmail;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public String getOwnerEmail() { return ownerEmail; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setHunger(int hunger) { this.hunger = hunger; }
    public void setHappiness(int happiness) { this.happiness = happiness; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}
