package cat.itacademy.s05.t02.controller.mapper;

import cat.itacademy.s05.t02.controller.dto.PetCreateRequest;
import cat.itacademy.s05.t02.controller.dto.PetResponse;
import cat.itacademy.s05.t02.persistence.entity.PetEntity;

public class PetMapper {

    private PetMapper() {}

    public static PetEntity toEntityCreate(PetCreateRequest req) {
        return PetEntity.builder()
                .name(req.getName())
                .color(req.getColor())
                .hunger(50)      // valores iniciales por defecto
                .happiness(50)
                .build();
    }

    public static PetResponse toResponse(PetEntity e) {
        return new PetResponse(
                e.getId(),
                e.getName(),
                e.getColor(),
                e.getHunger(),
                e.getHappiness(),
                e.getOwner() != null ? e.getOwner().getEmail() : null
        );
    }
}
