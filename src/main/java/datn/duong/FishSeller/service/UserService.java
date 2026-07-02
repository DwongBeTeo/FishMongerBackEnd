package datn.duong.FishSeller.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import datn.duong.FishSeller.dto.AuthDTO;
import datn.duong.FishSeller.dto.UserDTO;
import datn.duong.FishSeller.dto.password.ChangePasswordDTO;
import datn.duong.FishSeller.dto.password.ResetPasswordDTO;
import datn.duong.FishSeller.entity.PasswordResetTokenEntity;
import datn.duong.FishSeller.entity.RoleEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.PasswordResetTokenRepository;
import datn.duong.FishSeller.repository.RoleRepository;
import datn.duong.FishSeller.repository.UserRepository;
import datn.duong.FishSeller.util.EmailUtils;
import datn.duong.FishSeller.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.activation.url}")
    private String activationURL;

    // Add methods to handle profile-related operations: crud
    public UserDTO registerUser(UserDTO userDTO) {
        // kiểm tra xem email này đã được đăng ký chưa
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // if (userDTO.getPhoneNumber() != null &&
        // userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
        // throw new RuntimeException("Phone number already exists");
        // }
        // Kiểm tra và tạo role USER nếu chưa có
        RoleEntity defaultRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    RoleEntity newRole = RoleEntity.builder()
                            .name("USER")
                            .build();
                    return roleRepository.save(newRole);
                });
        UserEntity newUser = toEntity(userDTO);
        newUser.setRole(defaultRole);
        newUser.setActivationToken(UUID.randomUUID().toString());
        newUser = userRepository.save(newUser);
        log.info("Start send activate link");
        // Send activation email
        String activationLink = activationURL + "/api/v1.0/activate?token=" + newUser.getActivationToken();
        String subject = "Activate your money Manager account";
        String body = "Click on the following link to activate your account: " + activationLink;
        emailService.sendEmail(newUser.getEmail(), subject, body);
        log.info("Already send activate link to {}", newUser.getEmail());
        return toDTO(newUser);
    }

    // --- MỚI: HÀM LẤY DANH SÁCH USER KHẢ DỤNG CHO ADMIN ---
    public List<UserDTO> getAvailableUsersForEmployee() {
        // Gọi repository
        List<UserEntity> users = userRepository.findAvailableUsersForEmployee();
        
        // Convert sang DTO để trả về frontend (chỉ cần id, username, email, fullname)
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // helper method
    public UserEntity toEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .profileImageUrl(userDTO.getProfileImageUrl())
                .build();
    }

    public UserDTO toDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .fullName(userEntity.getFullName())
                .phoneNumber(userEntity.getPhoneNumber())
                .address(userEntity.getAddress())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .createdDate(userEntity.getCreatedDate())
                .updatedDate(userEntity.getUpdatedDate())
                .role(userEntity.getRole().getName())
                .build();
    }

    // active user
    public boolean activateUser(String activationToken) {
        return userRepository.findByActivationToken(activationToken)
                .map(user -> {
                    user.setIsActive(true);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    // check user actived or not
    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getIsActive)
                .orElse(false);
    }

    public UserEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Profile not foung with email: " + authentication.getName()));
    }

    // get public profile
    public UserDTO getPublicProfile(String email) {
        UserEntity currentUser;
        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }
        // assert currentUser != null; //có nên thêm dòng này không ?
        return UserDTO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .address(currentUser.getAddress())
                .phoneNumber(currentUser.getPhoneNumber())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .role(currentUser.getRole().getName())
                .build();
    }
    // METHOD UPDATE PROFILE ---
    public UserDTO updateUserProfile(UserDTO requestDTO) {
        // 1. Lấy user hiện tại đang đăng nhập
        UserEntity currentUser = getCurrentProfile();

        // 2. Cập nhật các thông tin cho phép
        
        if (requestDTO.getFullName() != null) {
            currentUser.setFullName(requestDTO.getFullName());
        }
        
        if (requestDTO.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(requestDTO.getPhoneNumber());
        }
        
        if (requestDTO.getAddress() != null) {
            currentUser.setAddress(requestDTO.getAddress());
        }

        if (requestDTO.getProfileImageUrl() != null) {
            currentUser.setProfileImageUrl(requestDTO.getProfileImageUrl());
        }

        // LOGIC ĐỔI MẬT KHẨU
        // Kiểm tra xem user có gửi password mới lên không và password đó có rỗng không
        if (requestDTO.getPassword() != null && !requestDTO.getPassword().trim().isEmpty()) {
            // QUAN TRỌNG: Phải mã hóa (Hash) password mới trước khi lưu
            // Nếu lưu trực tiếp requestDTO.getPassword() thì lần sau sẽ không login được
            currentUser.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        }

        // Lưu xuống DB
        UserEntity updatedUser = userRepository.save(currentUser);

        // Trả về DTO mới nhất
        return toDTO(updatedUser);
    }

    // 1. ĐỔI MẬT KHẨU (Change Password) - Đã login
    // =========================================================================
    public void changePassword(ChangePasswordDTO request) {
        UserEntity user = getCurrentProfile();

        // 1. Check mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        // 2. Check mật khẩu mới trùng mật khẩu cũ
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
             throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        // 3. Cập nhật mật khẩu mới (Encode)
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // =========================================================================
    // 2. QUÊN MẬT KHẨU (Forgot Password) Gửi otp
    // =========================================================================
    public void requestPasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này"));

        // 1. Xóa token/OTP cũ nếu tồn tại
        passwordResetTokenRepository.deleteByUser(user);

        // 2. Tạo OTP mới (6 số) thay vì UUID
        String otp = generateOTP();
        
        PasswordResetTokenEntity myToken = new PasswordResetTokenEntity(user, otp);
        passwordResetTokenRepository.save(myToken);

        // 3. Gửi Email chứa mã số
        String subject = "🔑 Mã xác thực đặt lại mật khẩu - Fish Seller";
        String htmlBody = EmailUtils.getOtpEmailTemplate(user.getUsername(), otp);
    
        emailService.sendEmail(user.getEmail(), subject, htmlBody);
    }

    // =========================================================================
    // 3. ĐẶT LẠI MẬT KHẨU (Reset Password) Xác nhận Token & Đổi pass
    // =========================================================================
    public void resetPassword(ResetPasswordDTO request) {
        // 1. Tìm User theo Email trước (Thay vì tìm theo Token)
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // 2. Tìm Token/OTP trong DB dựa trên User
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByUser(user) // Cần viết thêm hàm này trong Repo
                .orElseThrow(() -> new RuntimeException("Bạn chưa yêu cầu gửi mã OTP"));

        // 3. So sánh mã OTP người dùng nhập với mã trong DB
        if (!resetToken.getToken().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác");
        }

        // 4. Kiểm tra hết hạn
        Calendar cal = Calendar.getInstance();
        if ((resetToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }

        // 5. Đổi mật khẩu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 6. Xóa OTP sau khi dùng xong
        passwordResetTokenRepository.delete(resetToken);
    }

    // --- HELPER: SINH MÃ OTP 6 SỐ ---
    private String generateOTP() {
        // Sinh số ngẫu nhiên từ 0 đến 999999
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            System.out.println("Login email: " + authDTO.getEmail()); // Log để debug
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            // generate JWT token
            UserEntity user = userRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(
                            () -> new UsernameNotFoundException("Profile not found with email: " + authDTO.getEmail()));
            // Lấy role name
            String roleName = user.getRole().getName();
            String token = jwtUtil.generateToken(authDTO.getEmail(), roleName);
            System.out.println("Generated token for email: " + authDTO.getEmail()); // Log để debug
            UserDTO profile = getPublicProfile(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", profile);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Authentication failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new RuntimeException("Invalid email or password");
        }
    }
}
