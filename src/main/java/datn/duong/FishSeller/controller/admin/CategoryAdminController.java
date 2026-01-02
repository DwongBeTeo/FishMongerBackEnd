package datn.duong.FishSeller.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.CategoryDTO;
import datn.duong.FishSeller.service.CategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;

    // 1. Tạo danh mục mới
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    // 2. Cập nhật danh mục
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    // 3. Xóa danh mục (Đã an toàn để mở lại)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // 4. Lấy danh sách cho Admin (Dạng bảng phẳng)
    // Lưu ý: Đảm bảo Service của bạn đã đổi tên hàm thành getAllCategoriesForAdmin như bài trước
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        // Gọi hàm lấy list phẳng (Flat List) để hiển thị lên bảng Admin
        List<CategoryDTO> categories = categoryService.getAllCategoriesForAdmin();
        return ResponseEntity.ok(categories);
    }

    // 5. Lấy chi tiết 1 danh mục
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
}