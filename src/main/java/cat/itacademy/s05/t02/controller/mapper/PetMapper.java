package cat.itacademy.s05.t02.controller.mapper;

import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;

public class PetMapper {

    private PetMapper() {}

    // Optional: only if somewhere you create from DTO directly
    public static PetEntity toEntityCreate(PetCreateRequest req) {
        return PetEntity.builder()
                .name(req.getName())
                .color(req.getColor())
                .hunger(30)
                .stamina(70)
                .happiness(60)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .build();
    }

    public static PetResponse toResponse(PetEntity e) {
        PetResponse r = new PetResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setColor(e.getColor());
        r.setHunger(e.getHunger());
        r.setStamina(e.getStamina());
        r.setHappiness(e.getHappiness());
        r.setLevel(e.getLevel());
        r.setXpInLevel(e.getXpInLevel());
        r.setStage(e.getStage());
        r.setOwnerEmail(e.getOwner() != null ? e.getOwner().getEmail() : null);
        r.setImageUrl(pickImage(e.getStage()));
        return r;
    }

    private static String pickImage(EvolutionStage stage) {
        return switch (stage) {
            case BABY -> "/img/axolotl_baby.png";
            case TEEN -> "/img/axolotl_teen.png";
            case ADULT -> "/img/axolotl_adult.png";
        };
    }
}

