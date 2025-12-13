package datn.duong.FishSeller.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    PasswordResetTokenEntity findByToken(String token); // Để tìm token khi user nhập vào
    void deleteByUserId(Long userId); // Để xóa token cũ nếu user yêu cầu lại
}
