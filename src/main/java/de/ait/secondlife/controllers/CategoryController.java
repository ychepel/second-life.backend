package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.NewCategoryDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.dto.ValidationErrorsDto;
import de.ait.secondlife.services.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/categories")
@Tag(name = "Category controller", description = "Allow to perform CRUD operations for the categories")
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Get category by id", description = "Accessible to all users")
    @GetMapping("/{category-id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<CategoryDto> getById(@PathVariable("category-id") Long categoryId) {
        return ResponseEntity.ok(service.getById(categoryId));
    }

    @Operation(summary = "Get list of the categories", description = "Accessible to all users")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
    })
    public ResponseEntity<List<CategoryDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Add category", description = "Accessible only by admin")
    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict, category with this name already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))}
    )
    public ResponseEntity<CategoryDto> add(@Valid @RequestBody NewCategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
    }

    @Operation(summary = "Edit category", description = "Accessible only by admin")
    @PutMapping("/{category-id}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    }
    )
    public ResponseEntity<CategoryDto> update(@PathVariable("category-id") Long categoryId, @RequestBody @Valid CategoryDto dto) {
        return ResponseEntity.ok(service.update(categoryId, dto));
    }

    @Operation(summary = "Hiding category from the list of the categories", description = "Accessible only by admin, and only if the list of the offers related to this category is empty")
    @DeleteMapping("/{category-id}")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category hided",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    }
    )
    public ResponseEntity<CategoryDto> hideCategory(@PathVariable("category-id") Long categoryId) {
        CategoryDto hiddenCategory = service.hide(categoryId);
        return ResponseEntity.ok(hiddenCategory);
    }

    @Operation(summary = "Activating category with id", description = "Accessible only by admin")
    @PatchMapping("/{category-id}/set-active")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Category activated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404",
                    description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    }
    )
    public ResponseEntity<CategoryDto> setActive(@PathVariable("category-id") Long categoryId) {
        CategoryDto activeDto = service.setActive(categoryId);
        return ResponseEntity.ok(activeDto);
    }
}
