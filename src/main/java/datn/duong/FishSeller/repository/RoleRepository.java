package datn.duong.FishSeller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
