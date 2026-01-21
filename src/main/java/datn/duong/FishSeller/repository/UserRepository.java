package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    
    // MỚI: Tìm danh sách User có Role = 'USER' và chưa có trong bảng Employee
    // Giả sử tên bảng Entity của bạn là EmployeeEntity và UserEntity
    @Query("SELECT u FROM UserEntity u " +
           "WHERE u.role.name = 'USER' " +
           "AND u.id NOT IN (SELECT e.user.id FROM EmployeeEntity e)")
    List<UserEntity> findAvailableUsersForEmployee();
}
