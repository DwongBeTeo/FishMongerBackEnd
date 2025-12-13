package datn.duong.FishSeller.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // Lấy secret key từ application.properties
    private String secret;

    @Value("${jwt.expiration}") // Lấy thời gian hết hạn từ application.properties (tính bằng giây)
    private Long expiration;

    // Tạo JWT token dựa trên email
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email); // Lưu email vào claims
        claims.put("role", role);
        return createToken(claims, email);
    }

    // Tạo token với claims và subject
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // Xác thực token với email
    public Boolean validateToken(String token, UserDetails userDetail) {
        try {
            final String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(userDetail.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra token hợp lệ (Chỉ check chữ ký và hết hạn, không cần UserDetails từ DB)
    public boolean validateTokenSimple(String token) {
        try {
            // extractAllClaims sẽ ném Exception nếu chữ ký sai hoặc token lỗi
            extractAllClaims(token); 
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Trích xuất email từ token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Trích xuất role từ token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }


    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Trích xuất user ID từ token
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // Trích xuất claim cụ thể từ token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Trích xuất tất cả claims từ token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Kiểm tra token có hết hạn không
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Trích xuất ngày hết hạn từ token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}

