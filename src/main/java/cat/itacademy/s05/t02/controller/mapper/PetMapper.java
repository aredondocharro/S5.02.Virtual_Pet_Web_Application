package cat.itacademy.s05.t02.controller.mapper;

import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.domain.EvolutionStage;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;

public final class PetMapper {

    private PetMapper() {}

    public static PetEntity toEntityCreate(PetCreateRequest req) {
        return PetEntity.builder()
                .name(req.name())
                .color(req.color())
                .hunger(30)
                .stamina(70)
                .happiness(60)
                .level(1)
                .xpInLevel(0)
                .stage(EvolutionStage.BABY)
                .build();
    }

    public static PetResponse toResponse(PetEntity e) {
        return new PetResponse(
                e.getId(),
                e.getName(),
                e.getColor(),
                e.getHunger(),
                e.getStamina(),
                e.getHappiness(),
                e.getLevel(),
                e.getXpInLevel(),
                e.getStage(),
                e.getOwner() != null ? e.getOwner().getEmail() : null
        );
    }

}


