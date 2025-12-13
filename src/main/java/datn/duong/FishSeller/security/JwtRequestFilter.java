package datn.duong.FishSeller.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import datn.duong.FishSeller.util.JwtUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// Dùng để nhập token vào request trên header Authorization
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            jwt = authHeader.substring(7);
            try {
                email = jwtUtil.extractUserName(jwt);
            } catch (Exception e) {
                // TODO: handle exception
                throw new RuntimeException(e);
            }
        }
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            // Thay vì gọi DB, ta kiểm tra token và lấy Role từ token luôn
            if(jwtUtil.validateTokenSimple(jwt)) {
                // 1. Lấy Role từ Token
                String role = jwtUtil.extractRole(jwt);
                // 2. Tạo Authority (Quyền)
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                // 3. Tạo UserDetails "nhân tạo" (Không cần query DB)
                // Password để rỗng vì không cần check lại password ở đây
                UserDetails userDetails = new User(email, "", authorities);

                // 4. Tạo Authentication
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));;
                SecurityContextHolder.getContext().setAuthentication(authToken);;
            }
        }
        filterChain.doFilter(request,response);
    }
}

