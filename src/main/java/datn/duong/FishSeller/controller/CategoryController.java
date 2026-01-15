package datn.duong.FishSeller.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.CategoryDTO;
import datn.duong.FishSeller.service.CategoryService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/menu")
    public ResponseEntity<List<CategoryDTO>> getMenuCategories(String type) {
        List<CategoryDTO> categories = categoryService.getAllCategoriesForMenu(type);
        return ResponseEntity.ok(categories);
    }
}
