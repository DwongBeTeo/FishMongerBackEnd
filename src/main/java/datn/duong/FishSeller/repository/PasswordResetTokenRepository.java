package datn.duong.FishSeller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.entity.PasswordResetTokenEntity;
import datn.duong.FishSeller.entity.UserEntity;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    
    // Tìm kiếm token
    Optional<PasswordResetTokenEntity> findByUser(UserEntity user);
    
    // Xóa tất cả token cũ của user này (để tránh rác DB nếu họ request nhiều lần)
    @Modifying
    @Transactional
    void deleteByUser(UserEntity user); 
}
