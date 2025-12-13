package datn.duong.FishSeller.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with emial: " + email));

        // Kiểm tra role
        if (existingUser.getRole() == null) {
            throw new UsernameNotFoundException("Profile not found with emial: " + email);
        }
        // 1. Lấy tên role từ UserEntity
        String roleName = existingUser.getRole().getName();
        // 2. Tạo GrantedAuthority từ tên role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        System.out.println("Found user: " + existingUser.getEmail() + ", Role: " + roleName + ", Password: " + existingUser.getPassword());
        return User.builder()
                .username(existingUser.getEmail())
                .password(existingUser.getPassword())
                .authorities(Collections.singletonList(authority))
                .build();
    }
}
