package datn.duong.FishSeller.service;

import org.springframework.stereotype.Service;

import datn.duong.FishSeller.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
}
