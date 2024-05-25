package de.ait.secondlife.controllers;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.dto.IsActiveCategoryDto;
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
@RequestMapping("api/v1/categories")
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
    public ResponseEntity<CategoryDto> add(@RequestBody CategoryDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Edit category")
    @PutMapping("/{category-id}")
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id")Long categoryId, @RequestBody CategoryDto dto){
        return ResponseEntity.ok(service.update(categoryId,dto));
    }

    @Operation(summary = "Activating/Deactivating category with id", description = "To use PATCH method need send category id and true/false value in the request body of the active property")
    @PatchMapping("/{category-id}/setActive")
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id")Long categoryId,@RequestBody IsActiveCategoryDto dto){
        CategoryDto updatedDto = service.update(categoryId,dto);
        return ResponseEntity.ok(updatedDto);
    }
}
