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
        List<CategoryEntity> categories = categoryRepository.findAll();
        return categories.stream()
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

        // Lưu lại
        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        return toDTO(updatedCategory);
    }

    // 5. Xóa danh mục
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // Helper method
    private CategoryEntity toEntity(CategoryDTO categoryDTO) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
    }

    // Phương thức toDTO được sử dụng khi lấy dữ liệu từ Database để trả về cho người dùng (Frontend/Client).
    public CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
