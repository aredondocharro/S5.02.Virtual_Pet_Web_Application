package cat.itacademy.s05.t02.persistence.repository;

import cat.itacademy.s05.t02.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
}
