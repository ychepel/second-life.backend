package de.ait.secondlife.services;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.models.Category;
import de.ait.secondlife.repositories.CategoriesRepository;
import de.ait.secondlife.services.interfaces.CategoryService;
import de.ait.secondlife.services.mapping.CategoryMappingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private  CategoriesRepository repository;

    private CategoryMappingService mappingService;

    public CategoryServiceImpl(CategoriesRepository repository, CategoryMappingService mappingService) {
        this.repository = repository;
        this.mappingService = mappingService;
    }

    @Override
    public CategoryDto getById(Long id) {

        if (id == null || id <1){
            throw new RuntimeException("Category ID is incorrect");
        }

        Category category = repository.findById(id).orElse(null);

        if (category == null) {
            throw new RuntimeException("Category not found");
        }

        return mappingService.mapEntityToDto(category);
    }

    @Override
    public List<CategoryDto> getAll() {
        return repository.findAll()
                .stream()
                .filter(x-> x.isActive())
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public CategoryDto save(CategoryDto categoryDto) {

        Category entity = mappingService.mapDtoToEntity(categoryDto);

        try {
            repository.save(entity);
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db");
        }

        return mappingService.mapEntityToDto(entity);
    }

    @Override
    public void update(Long id, CategoryDto dto) {

        Category existingCategory = repository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());

        try {
            repository.save(existingCategory);
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db");
        }
    }
}
