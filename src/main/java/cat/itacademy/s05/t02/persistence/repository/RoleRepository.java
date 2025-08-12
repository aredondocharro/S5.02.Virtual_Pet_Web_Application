package cat.itacademy.s05.t02.persistence.repository;

import cat.itacademy.s05.t02.persistence.entity.RoleEntity;
import cat.itacademy.s05.t02.persistence.entity.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByRoleEnum(RoleEnum roleEnum);

    boolean existsByRoleEnum(RoleEnum roleEnum);

    List<RoleEntity> findByRoleEnumIn(List<RoleEnum> roles);
}
