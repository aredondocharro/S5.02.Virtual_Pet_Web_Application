package cat.itacademy.s05.t02.config.data.service;

import cat.itacademy.s05.t02.persistence.entity.PermissionEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEnum;
import cat.itacademy.s05.t02.persistence.repository.PermissionRepository;
import cat.itacademy.s05.t02.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeedService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public SeedService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void seedRolesAndPermissions() {
        // 1) UPSERT permisos (quedan managed dentro de la misma TX)
        PermissionEntity pCreate = upsertPermission("CREATE");
        PermissionEntity pRead   = upsertPermission("READ");
        PermissionEntity pUpdate = upsertPermission("UPDATE");
        PermissionEntity pDelete = upsertPermission("DELETE");

        // 2) UPSERT roles referenciando permisos MANAGED (reattach por nombre)
        upsertRole(RoleEnum.ADMIN,     Set.of("CREATE","READ","UPDATE","DELETE"));
        upsertRole(RoleEnum.USER,      Set.of("READ","UPDATE"));
        upsertRole(RoleEnum.GUEST,     Set.of("READ"));
        upsertRole(RoleEnum.DEVELOPER, Set.of("CREATE","READ","UPDATE"));
        upsertRole(RoleEnum.TESTER,    Set.of("READ","UPDATE","DELETE"));
    }

    private PermissionEntity upsertPermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(PermissionEntity.builder().name(name).build()));
    }

    private void upsertRole(RoleEnum roleEnum, Set<String> permissionNames) {
        if (roleRepository.findByRoleEnum(roleEnum).isPresent()) return;

        // Reobtener permisos MANAGED dentro de la misma transacción
        Set<PermissionEntity> managedPerms = permissionNames.stream()
                .map(n -> permissionRepository.findByName(n).orElseThrow())
                .collect(Collectors.toSet());

        RoleEntity role = RoleEntity.builder()
                .roleEnum(roleEnum)
                .permissionList(managedPerms)
                .build();

        roleRepository.save(role);
    }
}