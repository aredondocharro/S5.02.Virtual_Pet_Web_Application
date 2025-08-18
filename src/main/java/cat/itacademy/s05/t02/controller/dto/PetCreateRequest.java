package cat.itacademy.s05.t02.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload to create a pet")
public class PetCreateRequest {

    @Schema(description = "Visible name", example = "Neko", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "Main colour", example = "blue", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String color;

    public PetCreateRequest() {}

    public PetCreateRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
}