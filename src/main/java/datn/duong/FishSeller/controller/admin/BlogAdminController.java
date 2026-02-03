package datn.duong.FishSeller.controller.admin;

import datn.duong.FishSeller.dto.BlogCategoryDTO;
import datn.duong.FishSeller.dto.BlogPostDTO;
import datn.duong.FishSeller.service.BlogCategoryService;
import datn.duong.FishSeller.service.BlogPostService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class BlogAdminController {

    private final BlogCategoryService categoryService;
    private final BlogPostService blogPostService;

    // ================= CATEGORY MANAGEMENT =================

    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryDTO>> getCategories() {
        //phần này lấy các danh mục không bị xóa
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<BlogCategoryDTO> createCategory(@RequestBody BlogCategoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(dto));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<BlogCategoryDTO> updateCategory(@PathVariable Long id, @RequestBody BlogCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    // xóa mềm
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET /api/v1/admin/blog/categories/trash
    @GetMapping("/categories/trash")
    public ResponseEntity<List<BlogCategoryDTO>> getDeletedCategories() {
        return ResponseEntity.ok(categoryService.getDeletedCategories());
    }

    // PUT /api/v1/admin/blog/categories/{id}/restore
    @PutMapping("/categories/{id}/restore")
    public ResponseEntity<String> restoreCategory(@PathVariable Long id) {
        categoryService.restoreCategory(id);
        return ResponseEntity.ok("Khôi phục danh mục thành công!");
    }

    // ================= POST MANAGEMENT =================

    @PostMapping("/posts")
    public ResponseEntity<BlogPostDTO> createPost(@RequestBody BlogPostDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogPostService.createPost(dto));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<BlogPostDTO> updatePost(@PathVariable Long id, @RequestBody BlogPostDTO dto) {
        return ResponseEntity.ok(blogPostService.updatePost(id, dto));
    }

    // API này dùng để Xóa mềm hoặc Ẩn bài viết
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        // Bạn cần viết thêm hàm deletePost trong Service (set isDeleted=true)
        blogPostService.deletePost(id); 
        return ResponseEntity.noContent().build();
    }
    
    // API lấy tất cả bài viết (Kể cả bài ẩn) để Admin quản lý
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPostsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
         // Viết hàm getAllPostsAdmin trong Service
        return ResponseEntity.ok(blogPostService.getAllPostsAdmin(page, size));
    }

    // API: Khôi phục bài viết
    // PUT /api/v1/admin/blog/posts/{id}/restore
    @PutMapping("/posts/{id}/restore")
    public ResponseEntity<String> restorePost(@PathVariable Long id) {
        blogPostService.restorePost(id);
        return ResponseEntity.ok("Khôi phục bài viết thành công! Bài viết đang ở trạng thái Ẩn.");
    }

    // API: Xem thùng rác (Các bài đã xóa)
    // GET /api/v1/admin/blog/posts/trash
    @GetMapping("/posts/trash")
    public ResponseEntity<?> getDeletedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(blogPostService.getDeletedPosts(page, size));
    }
}