package cat.itacademy.s05.t02.controller.dto;

import cat.itacademy.s05.t02.domain.EvolutionStage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pet data returned by the API")
public class PetResponse {

    @Schema(description = "Pet ID", example = "42")
    private Long id;

    @Schema(description = "Display name of the pet", example = "Axo")
    private String name;

    @Schema(description = "Main color of the pet", example = "pink")
    private String color;

    @Schema(description = "Hunger 0..100 (0=satiated, 100=very hungry)", example = "40")
    private int hunger;

    @Schema(description = "Stamina 0..100 (0=exhausted, 100=full energy)", example = "55")
    private int stamina;

    @Schema(description = "Happiness 0..100", example = "70")
    private int happiness;

    @Schema(description = "Current level 1..15", example = "3")
    private int level;

    @Schema(description = "XP in the current level 0..100", example = "25")
    private int xpInLevel;

    @Schema(description = "Evolution stage derived from level", example = "BABY")
    private EvolutionStage stage;

    @Schema(description = "Owner email", example = "alex@example.com")
    private String ownerEmail;

    @Schema(description = "Frontend image URL suggestion derived from stage", example = "/img/axolotl_baby.png")
    private String imageUrl;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getHunger() { return hunger; }
    public void setHunger(int hunger) { this.hunger = hunger; }
    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }
    public int getHappiness() { return happiness; }
    public void setHappiness(int happiness) { this.happiness = happiness; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getXpInLevel() { return xpInLevel; }
    public void setXpInLevel(int xpInLevel) { this.xpInLevel = xpInLevel; }
    public EvolutionStage getStage() { return stage; }
    public void setStage(EvolutionStage stage) { this.stage = stage; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

