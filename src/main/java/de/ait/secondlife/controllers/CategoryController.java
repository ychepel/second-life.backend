package de.ait.secondlife.controllers;

import de.ait.secondlife.dto.CategoryDto;
import de.ait.secondlife.services.interfaces.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

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
    public ResponseEntity<Void> update(@PathVariable("category-id")Long categoryId, @RequestBody CategoryDto dto){
        service.update(categoryId,dto);
        return ResponseEntity.noContent().build();
    }

    //TODO написать пут запрос на изменение isActive передавая в бади тру либо фолс
}
