package cat.itacademy.s05.t02.controller.mapper;

import cat.itacademy.s05.t02.controller.dto.UserProfileResponse;
import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.UserEntity;

import java.util.List;

public final class UserMapper {
    private UserMapper() {}

    public static UserProfileResponse toProfile(UserEntity u) {
        List<String> roles = u.getRoles() == null ? List.of()
                : u.getRoles().stream()
                .map(RoleEntity::getRoleEnum)
                .map(Enum::name)
                .toList();

        return new UserProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getUsername(),
                u.getAvatarUrl(),   // <-- nuevo campo en entidad
                u.getBio(),         // <-- nuevo campo en entidad
                roles
        );
    }
}
