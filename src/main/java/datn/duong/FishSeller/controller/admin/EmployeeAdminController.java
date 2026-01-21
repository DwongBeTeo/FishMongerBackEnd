package datn.duong.FishSeller.controller.admin;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.EmployeeDTO;
import datn.duong.FishSeller.enums.EmployeeStatus;
import datn.duong.FishSeller.service.EmployeeService;

@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeAdminController {

    private final EmployeeService employeeService;

    // 1. Lấy danh sách (Search + Filter)
    // URL: GET /admin/employees?keyword=hung&status=ACTIVE
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.getAllEmployees(keyword, status, page, size));
    }

    // 2. Tạo nhân viên mới (Gán User làm nhân viên)
    // URL: POST /admin/employees
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(dto));
    }

    // 3. Cập nhật nhân viên
    // URL: PUT /admin/employees/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id, 
            @RequestBody EmployeeDTO dto
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }
    
    // 4. Xem chi tiết
    // URL: GET /admin/employees/profile (Nếu muốn xem profile của chính mình khi login là NV)
    @GetMapping("/profile")
    public ResponseEntity<EmployeeDTO> getMyProfile() {
        return ResponseEntity.ok(employeeService.getMyProfile());
    }

    // 5. API lấy danh sách nhân viên đang làm việc (cho dropdown phân công)
    // URL: GET /admin/employees/active
    @GetMapping("/active")
    public ResponseEntity<List<EmployeeDTO>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }
}
