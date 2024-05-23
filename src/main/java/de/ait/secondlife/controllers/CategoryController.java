package de.ait.secondlife.controllers;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.dto.IsActiveCategoryDto;
import de.ait.secondlife.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    @GetMapping("/{category-id}")
    public ResponseEntity<CategoryDto> getById(@PathVariable("category-id")Long categoryId){
        return ResponseEntity.ok(service.getById(categoryId));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> add(@RequestBody CategoryDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @PutMapping("/{category-id}")
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id")Long categoryId, @RequestBody CategoryDto dto){
        return ResponseEntity.ok(service.update(categoryId,dto));
    }

    @PatchMapping("/{category-id}/setActive")
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id")Long categoryId,@RequestBody IsActiveCategoryDto dto){
        CategoryDto updatedDto = service.update(categoryId,dto);
        return ResponseEntity.ok(updatedDto);
    }
}
