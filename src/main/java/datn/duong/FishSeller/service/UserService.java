package datn.duong.FishSeller.service;

import java.util.Map;
import java.util.UUID;

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
import datn.duong.FishSeller.entity.RoleEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.RoleRepository;
import datn.duong.FishSeller.repository.UserRepository;
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

    @Value("${app.activation.url}")
    private String activationURL;

    // Add methods to handle profile-related operations: crud
    public UserDTO registerUser(UserDTO userDTO) {
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

    // helper method
    public UserEntity toEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
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

    // check user active
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
        // assert currentUser != null;
        return UserDTO.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                // .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .address(currentUser.getAddress())
                .phoneNumber(currentUser.getPhoneNumber())
                // .profileImageUrl(currentUser.getProfileImageUrl())
                // .createdAt(currentUser.getCreatedAt())
                // .updateAt(currentUser.getUpdateAt())
                .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            System.out.println("Login email: " + authDTO.getEmail()); // Log để debug
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            // generate JWT token
            UserEntity user = userRepository.findByEmail(authDTO.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authDTO.getEmail()));
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
