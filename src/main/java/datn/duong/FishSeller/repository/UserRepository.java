package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    // kiểm tra xem có trùng username không
    // Boolean existsByUsername(String username);

    // kiem tra xem email nay da duoc dang ky chua
    Boolean existsByEmail(String email);

    // check phone number có tồn tại hãy chưa 
    boolean existsByPhoneNumber(String phoneNumber);

    //select * from tbl_profiles where email = ?
    Optional<UserEntity> findByEmail(String email);

    //select * from tbl_profiles where activation_token = ?
    Optional<UserEntity> findByActivationToken(String activationToken);
}
