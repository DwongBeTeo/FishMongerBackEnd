package datn.duong.FishSeller.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import datn.duong.FishSeller.dto.CategoryDTO;
import datn.duong.FishSeller.entity.CategoryEntity;
import datn.duong.FishSeller.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // PHẦN 1: PUBLIC METHODS (Ai cũng dùng được: Khách, User, Admin)
    // 1. Lấy tất cả danh mục
    public List<CategoryDTO> getAllCategories() {
        // Chỉ lấy các danh mục gốc (Cha là null) để đệ quy xuống con
        // Nếu lấy findAll() thì con sẽ bị lặp lại ở cấp 1
        List<CategoryEntity> rootCategories = categoryRepository.findAllByParentIsNull();
        // *Lưu ý: Bạn cần viết thêm hàm findAllByParentIsNull() trong Repository

        // Nếu bạn muốn lấy list phẳng (admin table) thì dùng findAll() bình thường
        // List<CategoryEntity> categories = categoryRepository.findAll();
        return rootCategories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 2. Lấy chi tiết 1 danh mục theo ID
    public CategoryDTO getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDTO(category);
    }

    // PHẦN 2: ADMIN METHODS (Chỉ Admin dùng: Thêm, Sửa, Xóa)
    // 3. Tạo danh mục mới
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Kiểm tra trùng tên
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        CategoryEntity newCategory = toEntity(categoryDTO);
        // Xử lý logic gán Cha (Parent) - Phần này toEntity không làm được
        if (categoryDTO.getParentId() != null) {
            CategoryEntity parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            newCategory.setParent(parent);
        }
        CategoryEntity savedCategory = categoryRepository.save(newCategory);
        return toDTO(savedCategory);
    }

    // 4. Cập nhật danh mục
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        CategoryEntity existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Cập nhật thông tin
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

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
        return toDTO(updatedCategory);
    }

    // 5. Xóa danh mục
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

    // Helper method
    // (Chỉ map thông tin cơ bản, KHÔNG map quan hệ cần query DB)
    private CategoryEntity toEntity(CategoryDTO categoryDTO) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .isDeleted(false)
                // Không map parentId ở đây vì Entity cần Object Parent,
                // việc tìm Parent phải dùng Repository ở Service chính.
                .build();
    }

    // Phương thức toDTO được sử dụng khi lấy dữ liệu từ Database để trả về cho
    // người dùng (Frontend/Client).
    public CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .name(entity.getName())
                .description(entity.getDescription())
                .isDeleted(entity.getIsDeleted())
                // 1. Xử lý Parent (Lấy ID và Tên cha)
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)

                // 2. Xử lý Product Count (Dùng .size() là cách đơn giản nhất)
                // Lưu ý: Cần @Transactional ở method gọi hàm này để fetch data từ DB
                .productCount(entity.getProducts() != null ? (long) entity.getProducts().size() : 0L)

                // 3. Xử lý Children (Đệ quy)
                // Dùng stream gọi lại chính hàm toDTO này cho các con
                .children(entity.getChildren() != null
                        ? entity.getChildren().stream().map(this::toDTO).collect(Collectors.toList())
                        : null)
                .build();
    }
}
