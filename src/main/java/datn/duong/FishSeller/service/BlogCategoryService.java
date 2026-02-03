package datn.duong.FishSeller.service;

import datn.duong.FishSeller.dto.BlogCategoryDTO;
import datn.duong.FishSeller.entity.BlogCategoryEntity;
import datn.duong.FishSeller.repository.BlogCategoryRepository;
import datn.duong.FishSeller.repository.BlogPostRepository;
import datn.duong.FishSeller.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCategoryService {

    private final BlogCategoryRepository categoryRepository;
    private final BlogPostRepository blogPostRepository; // Inject thêm repo bài viết
    // USER
    // Chỉ trả về các danh mục đang hoạt động
    public List<BlogCategoryDTO> getPublicCategories() {
        return categoryRepository.findAllByIsActiveTrueAndIsDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ADMIN
    // 1. Lấy tất cả
    public List<BlogCategoryDTO> getAllCategories() {
        return categoryRepository.findAllByIsDeletedFalse().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 2. Tạo mới
    public BlogCategoryDTO createCategory(BlogCategoryDTO dto) {
        BlogCategoryEntity entity = toEntity(dto);
        // Tự sinh slug nếu không gửi lên
        if (entity.getSlug() == null || entity.getSlug().isEmpty()) {
            entity.setSlug(SlugUtil.makeSlug(entity.getName()));
        }
        // Check trùng slug (quan trọng cho SEO)
        if (categoryRepository.existsBySlug(entity.getSlug())) {
            entity.setSlug(entity.getSlug() + "-" + System.currentTimeMillis());
        }
        
        return toDTO(categoryRepository.save(entity));
    }

    // 3. Cập nhật
    @Transactional
    public BlogCategoryDTO updateCategory(Long id, BlogCategoryDTO dto) {
        BlogCategoryEntity existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        // Nếu muốn cho sửa slug thì uncomment dòng dưới
        existing.setSlug(SlugUtil.makeSlug(dto.getName())); 
        
        // Logic chặn ẩn nếu đang có bài viết
        if (dto.getIsActive() != null && !dto.getIsActive()) {
            if (blogPostRepository.existsByCategoryIdAndIsActiveTrue(id)) {
                throw new RuntimeException("Không thể ẩn danh mục này vì đang có bài viết hoạt động!");
            }
            existing.setIsActive(false);
        } else if (dto.getIsActive() != null) {
             existing.setIsActive(dto.getIsActive());
        }

        return toDTO(categoryRepository.save(existing));
    }

    // 4. Xóa mềm (Chặn nếu có bài viết)
    @Transactional
    public void deleteCategory(Long id) {
        // Kiểm tra xem có bài viết nào thuộc danh mục này không (kể cả bài đã ẩn cũng tính)
        if (blogPostRepository.existsByCategoryId(id)) {
            throw new RuntimeException("Can not delete this BlogCategory. Please delete all related BlogPosts first.");
        }

        BlogCategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        
        category.setIsDeleted(true);
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    // 5. Khôi phục danh mục
    @Transactional
    public void restoreCategory(Long id) {
        BlogCategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        if (!category.getIsDeleted()) {
            throw new RuntimeException("Danh mục này chưa bị xóa!");
        }

        category.setIsDeleted(false);
        // Lưu ý: Vẫn để isActive = false để Admin tự bật lại sau khi kiểm tra
        category.setIsActive(true); 

        categoryRepository.save(category);
    }

    // 6. Xem thùng rác danh mục
    public List<BlogCategoryDTO> getDeletedCategories() {
        return categoryRepository.findAllByIsDeletedTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---
    private BlogCategoryEntity toEntity(BlogCategoryDTO dto) {
        return BlogCategoryEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .metaTitle(dto.getMetaTitle())
                .metaKeyword(dto.getMetaKeyword())
                .metaDescription(dto.getMetaDescription())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }

    private BlogCategoryDTO toDTO(BlogCategoryEntity entity) {
        return BlogCategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .postCount(entity.getPosts().size()) // Đếm số bài viết
                .build();
    }
}