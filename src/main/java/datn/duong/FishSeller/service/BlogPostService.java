package datn.duong.FishSeller.service;

import datn.duong.FishSeller.dto.BlogPostDTO;
import datn.duong.FishSeller.entity.BlogCategoryEntity;
import datn.duong.FishSeller.entity.BlogPostEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.BlogCategoryRepository;
import datn.duong.FishSeller.repository.BlogPostRepository;
import datn.duong.FishSeller.util.SlugUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final UserService userService;

    // User
    // 1. Lấy danh sách bài viết cho Public (Chỉ lấy Active = true, Deleted = false)
    public Page<BlogPostDTO> getPublicPosts(Long categoryId, Pageable pageable) {
        Page<BlogPostEntity> pageEntities;
        if (categoryId != null) {
            pageEntities = blogPostRepository.findAllByCategoryIdAndIsActiveTrueAndIsDeletedFalse(categoryId, pageable);
        } else {
            pageEntities = blogPostRepository.findAllByIsActiveTrueAndIsDeletedFalse(pageable);
        }
        return pageEntities.map(this::toDTO);
    }

    // Admin
    // 1. Tạo bài viết mới
    @Transactional
    public BlogPostDTO createPost(BlogPostDTO dto) {
        UserEntity currentUser = userService.getCurrentProfile();
        
        BlogCategoryEntity category = blogCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        BlogPostEntity entity = toEntity(dto);
        
        // Gán các thông tin hệ thống
        entity.setCategory(category);
        entity.setCreatedBy(currentUser.getId());
        
        // Xử lý Slug
        if (entity.getSlug() == null || entity.getSlug().isEmpty()) {
            entity.setSlug(SlugUtil.makeSlug(entity.getTitle()));
        }
        if (blogPostRepository.existsBySlug(entity.getSlug())) {
             entity.setSlug(entity.getSlug() + "-" + System.currentTimeMillis());
        }

        return toDTO(blogPostRepository.save(entity));
    }

    // 2. Cập nhật bài viết
    @Transactional
    public BlogPostDTO updatePost(Long id, BlogPostDTO dto) {
        BlogPostEntity existing = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        
        // Update thông tin cơ bản
        existing.setTitle(dto.getTitle());
        existing.setThumbnail(dto.getThumbnail());
        existing.setShortDescription(dto.getShortDescription());
        existing.setContent(dto.getContent());
        existing.setUpdatedBy(userService.getCurrentProfile().getId());
        
        // Update SEO
        existing.setMetaTitle(dto.getMetaTitle());
        existing.setMetaKeyword(dto.getMetaKeyword());
        existing.setMetaDescription(dto.getMetaDescription());
        
        // Update trạng thái
        if (dto.getIsActive() != null) existing.setIsActive(dto.getIsActive());
        if (dto.getIsHome() != null) existing.setIsHome(dto.getIsHome());

        // Update Category nếu thay đổi
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(existing.getCategory().getId())) {
            BlogCategoryEntity newCat = blogCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục mới không tồn tại"));
            existing.setCategory(newCat);
        }

        return toDTO(blogPostRepository.save(existing));
    }

    // 3. Get chi tiết (kèm tăng view)
    @Transactional
    public BlogPostDTO getPostDetail(String slug) {
        BlogPostEntity post = blogPostRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Bài viết không tìm thấy"));
        
        // Tăng view
        post.setViewCount(post.getViewCount() + 1);
        blogPostRepository.save(post);
        
        return toDTO(post);
    }

    // 4. Lấy danh sách bài viết cho Admin (Lấy tất cả trừ Deleted)
    public Page<BlogPostDTO> getAllPostsAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return blogPostRepository.findAllByIsDeletedFalse(pageable).map(this::toDTO);
    }
    
    // 5. Xóa bài viết (Soft Delete)
    @Transactional
    public void deletePost(Long id) {
        BlogPostEntity post = blogPostRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setIsDeleted(true);
        post.setIsActive(false); // Ẩn luôn cho chắc
        blogPostRepository.save(post);
    }
    
    // 6. Lấy bài viết nổi bật (IsHome = true)
    public List<BlogPostDTO> getFeaturedPosts() {
        // Lấy top 5 bài mới nhất được đánh dấu lên trang chủ
        return blogPostRepository.findTop5ByIsHomeTrueAndIsActiveTrueAndIsDeletedFalseOrderByCreatedDateDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // 7. Khôi phục bài viết (Chuyển isDeleted về false)
    @Transactional
    public void restorePost(Long id) {
        // Tìm bài viết (kể cả bài đã bị đánh dấu xóa)
        BlogPostEntity post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        if (!post.getIsDeleted()) {
            throw new RuntimeException("Bài viết này chưa bị xóa, không cần khôi phục!");
        }

        // Logic khôi phục:
        post.setIsDeleted(false);
        
        // LƯU Ý QUAN TRỌNG: 
        // Khi khôi phục, nên để isActive = false (về dạng Nháp/Draft).
        // Để Admin kiểm tra lại nội dung rồi mới bấm "Hiện" thủ công. 
        // Tránh trường hợp bài cũ nội dung lỗi thời hiện ra làm khách hiểu nhầm.
        post.setIsActive(false); 

        blogPostRepository.save(post);
    }

    // 8. Lấy danh sách bài viết trong "Thùng rác" (Đã xóa mềm)
    public Page<BlogPostDTO> getDeletedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedDate").descending());
        return blogPostRepository.findAllByIsDeletedTrue(pageable).map(this::toDTO);
    }
    
    // --- Helper Methods ---
    private BlogPostEntity toEntity(BlogPostDTO dto) {
        return BlogPostEntity.builder()
                .title(dto.getTitle())
                .thumbnail(dto.getThumbnail())
                .shortDescription(dto.getShortDescription())
                .content(dto.getContent())
                .isHome(dto.getIsHome() != null ? dto.getIsHome() : false)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .metaTitle(dto.getMetaTitle())
                .metaKeyword(dto.getMetaKeyword())
                .metaDescription(dto.getMetaDescription())
                .build();
    }

    private BlogPostDTO toDTO(BlogPostEntity entity) {
        return BlogPostDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .thumbnail(entity.getThumbnail())
                .shortDescription(entity.getShortDescription())
                .content(entity.getContent())
                .categoryId(entity.getCategory().getId())
                .categoryName(entity.getCategory().getName())
                .viewCount(entity.getViewCount())
                .createdBy(entity.getCreatedBy())
                // .authorName(...) // Có thể gọi userRepo để lấy tên nếu cần
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .isActive(entity.getIsActive())
                .isHome(entity.getIsHome())
                .build();
    }
}