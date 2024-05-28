package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.services.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/categories")
@Tag(name = "Category controller")
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Get category by id")
    @GetMapping("/{category-id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable("category-id")Long categoryId){
        return ResponseEntity.ok(service.getById(categoryId));
    }

    @Operation(summary = "Get list of the categories")
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Add category")
    @PostMapping
    public ResponseEntity<CategoryDto> add(@RequestBody NewCategoryDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{category-id}")
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id")Long categoryId, @RequestBody CategoryDto dto){
        return ResponseEntity.ok(service.update(categoryId,dto));
    }

    @Operation(summary = "Hiding category from the list of the categories", description = "Allowed only if the list of the offers related to this category is empty")
    @DeleteMapping("/{category-id}")
    public ResponseEntity<CategoryDto> hideCategory(@PathVariable("category-id")Long categoryId){
        CategoryDto hiddenCategory = service.hide(categoryId);
        return ResponseEntity.ok(hiddenCategory);
    }

    @Operation(summary = "Activating category with id")
    @PatchMapping("/{category-id}/set-active")
    public ResponseEntity<CategoryDto> setActive(@PathVariable("category-id")Long categoryId){
        CategoryDto activeDto = service.setActive(categoryId);
        return ResponseEntity.ok(activeDto);
    }
}
