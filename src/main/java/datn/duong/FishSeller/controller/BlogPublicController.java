package datn.duong.FishSeller.controller;

import datn.duong.FishSeller.dto.BlogCategoryDTO;
import datn.duong.FishSeller.dto.BlogPostDTO;
import datn.duong.FishSeller.service.BlogCategoryService;
import datn.duong.FishSeller.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogPublicController {

    private final BlogCategoryService categoryService;
    private final BlogPostService blogPostService;

    // 1. Lấy danh sách Categories (Để hiển thị menu lọc)
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryDTO>> getCategories() {
        return ResponseEntity.ok(categoryService.getPublicCategories()); 
    }

    // 2. Lấy danh sách bài viết (Có phân trang & Lọc theo Category)
    // VD: /blog/posts?page=0&size=10&categoryId=1
    @GetMapping("/posts")
    public ResponseEntity<Page<BlogPostDTO>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        // Bạn cần viết thêm hàm getPostsPublic trong Service để gọi Repository
        // Hàm này chỉ lấy bài có isActive = true
        return ResponseEntity.ok(blogPostService.getPublicPosts(categoryId, pageable));
    }

    // 3. Xem chi tiết bài viết (Theo Slug)
    @GetMapping("/posts/{slug}")
    public ResponseEntity<BlogPostDTO> getPostDetail(@PathVariable String slug) {
        return ResponseEntity.ok(blogPostService.getPostDetail(slug));
    }
    
    // 4. Lấy bài viết nổi bật (Cho trang chủ)
    @GetMapping("/posts/featured")
    public ResponseEntity<List<BlogPostDTO>> getFeaturedPosts() {
        return ResponseEntity.ok(blogPostService.getFeaturedPosts());
    }
}