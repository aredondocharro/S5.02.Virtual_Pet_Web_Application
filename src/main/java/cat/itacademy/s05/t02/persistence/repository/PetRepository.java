package cat.itacademy.s05.t02.persistence.repository;

import cat.itacademy.s05.t02.persistence.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<PetEntity, Long> {
    List<PetEntity> findByOwnerEmail(String email);
    boolean existsByIdAndOwnerEmail(Long id, String email);
}