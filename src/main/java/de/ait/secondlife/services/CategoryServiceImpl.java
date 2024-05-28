package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.domain.entity.Category;
import de.ait.secondlife.repositories.CategoriesRepository;
import de.ait.secondlife.services.interfaces.CategoryService;
import de.ait.secondlife.services.mapping.NewCategoryMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoriesRepository repository;

    private final NewCategoryMappingService mappingService;

    @Override
    public CategoryDto getById(Long id) {

        if (id == null || id <1){
            throw new RuntimeException("Category ID is incorrect");
        }

        Category category = repository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

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
    public CategoryDto save(NewCategoryDto categoryDto) {

        Category entity = mappingService.mapDtoToEntity(categoryDto);

        try {
            repository.save(entity);
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db");
        }

        return mappingService.mapEntityToDto(entity);
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {

        Category existingCategory = repository.findById(id).orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        existingCategory.setName(dto.getName());
        existingCategory.setDescription(dto.getDescription());

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db");
        }
    }

    @Override
    public CategoryDto setActive(Long categoryId) {

        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        existingCategory.setActive(true);

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db ", e);
        }
    }

    @Override
    public CategoryDto hide(Long categoryId) {
        Category existingCategory = repository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));


        //TODO check if the list of the offers related to this category is empty

        existingCategory.setActive(false);

        try {
            return mappingService.mapEntityToDto(repository.save(existingCategory));
        }catch (Exception e){
            throw new RuntimeException("Cannot save category to db ", e);
        }
    }
}
