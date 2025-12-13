package datn.duong.FishSeller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    // kiểm tra xem có trùng username không
    // Boolean existsByUsername(String username);

    //select * from tbl_profiles where email = ?
    Optional<UserEntity> findByEmail(String email);

    //select * from tbl_profiles where activation_token = ?
    Optional<UserEntity> findByActivationToken(String activationToken);
}
