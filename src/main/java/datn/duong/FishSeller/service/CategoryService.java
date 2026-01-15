package datn.duong.FishSeller.service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.CategoryDTO;
import datn.duong.FishSeller.entity.CategoryEntity;
import datn.duong.FishSeller.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // PHẦN 1: PUBLIC METHODS (Ai cũng dùng được: Khách, User, Admin)
    // (QUAN TRỌNG: Cần @Transactional cho Lazy Loading)
    // 1. Lấy tất cả danh mục
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesForAdmin(String keyword) {
        List<CategoryEntity> categories;

        // Nếu có từ khóa tìm kiếm -> Gọi hàm tìm kiếm Native (tìm cả đã xóa)
        if (keyword != null && !keyword.trim().isEmpty()) {
            categories = categoryRepository.searchForAdminRaw(keyword.trim());
        } 
        // Nếu không có từ khóa -> Gọi hàm findAll Native (lấy cả đã xóa)
        else {
            categories = categoryRepository.findAllForAdminRaw();
        }

        return categories.stream()
                .map(category -> toDTO(category, false)) 
                .collect(Collectors.toList());
    }

    // Dùng cho USER/MENU (Hiển thị dạng cây phân cấp)
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategoriesForMenu(String type) {
        // Chỉ lấy gốc, sau đó đệ quy lấy con
        List<CategoryEntity> rootCategories = categoryRepository.findByParentIsNotNullAndType(type);
        return rootCategories.stream()
                .map(category -> toDTO(category, true)) // true: Cần load con đệ quy
                .collect(Collectors.toList());
    }

    // 2. Lấy chi tiết 1 danh mục theo ID
    public CategoryDTO getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDTO(category,true);
    }

    // PHẦN 2: ADMIN METHODS (Chỉ Admin dùng: Thêm, Sửa, Xóa)
    // 3. Tạo danh mục mới
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Kiểm tra trùng tên
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        // Xử lý SLUG (Quan trọng)
        String slug = categoryDTO.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            // Nếu không nhập slug -> Tự tạo từ Name
            slug = toSlug(categoryDTO.getName());
        }
        
        // Kiểm tra trùng Slug
        if (categoryRepository.existsBySlug(slug)) {
            throw new RuntimeException("Slug '" + slug + "' đã tồn tại, vui lòng chọn tên khác hoặc sửa slug.");
        }

        CategoryEntity newCategory = toEntity(categoryDTO);
        newCategory.setSlug(slug); // Set slug đã xử lý

        // Xử lý logic gán Cha (Parent) - Phần này toEntity không làm được
        if (categoryDTO.getParentId() != null) {
            CategoryEntity parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            newCategory.setParent(parent);
        }
        CategoryEntity savedCategory = categoryRepository.save(newCategory);
        return toDTO(savedCategory,false);
    }

    // 4. Cập nhật danh mục
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        CategoryEntity existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Cập nhật thông tin
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        existingCategory.setType(categoryDTO.getType());
        existingCategory.setMetaTitle(categoryDTO.getMetaTitle());
        existingCategory.setMetaKeyword(categoryDTO.getMetaKeyword());

        // Xử lý update Slug (Khá phức tạp vì cần check trùng, trừ chính nó ra)
        String newSlug = categoryDTO.getSlug();
        if (newSlug == null || newSlug.trim().isEmpty()) {
             newSlug = toSlug(categoryDTO.getName());
        }
        
        // Nếu slug thay đổi, phải check trùng
        if (!newSlug.equals(existingCategory.getSlug())) {
            if (categoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                throw new RuntimeException("Slug '" + newSlug + "' đã được sử dụng bởi danh mục khác.");
            }
            existingCategory.setSlug(newSlug);
        }

        // Update quan hệ Cha - Con (Di chuyển danh mục)
        if (categoryDTO.getParentId() == null) {
            existingCategory.setParent(null); // Cho làm gốc
        } else {
            // Kiểm tra: Không được chọn chính mình làm cha
            if (categoryDTO.getParentId().equals(id)) {
                throw new RuntimeException("Cannot set category itself as parent");
            }

            CategoryEntity newParent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            existingCategory.setParent(newParent);
        }

        // Lưu lại
        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        return toDTO(updatedCategory,false);
    }

    // 5. Xóa danh mục(xóa mềm)
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        // Kiểm tra nếu còn con thì không cho xóa
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục vì vẫn còn danh mục con bên trong!");
        }
        // Kiểm tra nếu còn sản phẩm thì không cho xóa (như đã bàn ở câu trước)
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục vì vẫn còn sản phẩm!");
        }
        categoryRepository.deleteById(category.getId());
    }

    // 6. khôi phục danh mục
    public void restoreCategory(Long id) {
        //Tìm danh mục (Bắt buộc dùng hàm native query vừa viết ở trên)
        CategoryEntity category = categoryRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với id: " + id));

        //Kiểm tra nếu chưa xóa thì báo lỗi hoặc return luôn
        if (!category.getIsDeleted()) {
            throw new RuntimeException("Danh mục này chưa bị xóa, không cần khôi phục!");
        }

        //Logic nghiệp vụ nâng cao - Kiểm tra cha
        // Nếu danh mục này có cha, và cha đang bị xóa, thì không thể khôi phục con "mồ côi"
        if (category.getParent() != null) {
            CategoryEntity parent = categoryRepository.findByIdIncludingDeleted(category.getParent().getId())
                    .orElse(null);
            
            if (parent != null && parent.getIsDeleted()) {
                throw new RuntimeException("Không thể khôi phục danh mục vì danh mục cha đang bị xóa. Vui lòng khôi phục cha trước!");
            }
        }

        //Thực hiện khôi phục
        category.setIsDeleted(false);
        categoryRepository.save(category);
    }

    // Helper method

    // Hàm chuyển Tiếng Việt có dấu -> Slug không dấu (VD: "Cá Cảnh" -> "ca-canh")
    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
        
    }
    // (Chỉ map thông tin cơ bản, KHÔNG map quan hệ cần query DB)
    private CategoryEntity toEntity(CategoryDTO categoryDTO) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .type(categoryDTO.getType())
                .metaTitle(categoryDTO.getMetaTitle())
                .metaKeyword(categoryDTO.getMetaKeyword())
                .isDeleted(false)
                // Không map parentId ở đây vì Entity cần Object Parent,
                // việc tìm Parent phải dùng Repository ở Service chính.
                .build();
    }

    // Phương thức toDTO được sử dụng khi lấy dữ liệu từ Database để trả về cho
    // người dùng (Frontend/Client).Thêm tham số loadChildren để kiểm soát đệ quy
    public CategoryDTO toDTO(CategoryEntity entity, boolean loadChildren) {
        CategoryDTO.CategoryDTOBuilder builder = CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .isDeleted(entity.getIsDeleted())
                .type(entity.getType())
                .slug(entity.getSlug())
                .metaTitle(entity.getMetaTitle())
                .metaKeyword(entity.getMetaKeyword())
                // Lấy thông tin cha (để hiển thị trên bảng Admin)
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null);

        // Map số lượng sản phẩm (Cẩn thận Lazy Loading -> Cần @Transactional ở hàm gọi)
        if (entity.getProducts() != null) {
            builder.productCount((long) entity.getProducts().size());
        }

        // Chỉ load con nếu cần (Menu cần, Bảng Admin không cần)
        if (loadChildren && entity.getChildren() != null) {
            builder.children(entity.getChildren().stream()
                    .map(child -> toDTO(child, true)) // Đệ quy tiếp
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
