package datn.duong.FishSeller.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.AuthDTO;
import datn.duong.FishSeller.dto.UserDTO;
import datn.duong.FishSeller.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
// @RequestMapping("")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody UserDTO userDTO ){
        try {
            //Chuyển UserDTO thành UserEntity (bằng phương thức toEntity).
            //Trả về UserDTO (được gán vào registeredProfile).
            UserDTO registeredProfile = userService.registerUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
            
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam String token){
        boolean isActivated = userService.activateUser(token);
        if (isActivated){
            return ResponseEntity.ok("Profile activated successfully, Please comeback your website");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            if(!userService.isAccountActive(authDTO.getEmail())){

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account first"
                ));
            }
            Map<String, Object> response = userService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/test")
    public String test() {
        return "test successful";
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getPublicProfile () {
        UserDTO userDTO =  userService.getPublicProfile(null);
        return ResponseEntity.ok(userDTO);
    }
}
