package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.ImageCreateDto;
import de.ait.secondlife.domain.dto.ImageRequestDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.services.interfaces.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image controller", description = "Controller for some operations with images")
public class ImageContorller {

    private final ImageService imageService;

    @PostMapping("/upload")
    @Operation(
            summary = "Create new image",
            description = "Create new image for entity by entity id. " +
                    "The maximum allowable image upload size is 8MB. " +
                    "The maximum number of images for the Offer entity is 5. " +
                    "For Category and User - 1."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            )),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            ))})
    public ResponseEntity<ResponseMessageDto> uploadImage(
            @Valid
            @Parameter(description = "Dto with image file, entity type and entity id ", schema = @Schema(implementation = ImageCreateDto.class))
            ImageCreateDto request) {
        imageService.saveNewImage(request);
        return ResponseEntity.ok(
                new ResponseMessageDto("Image(s) successful saved"));
    }

    @DeleteMapping
    @Operation(
            summary = "Delete image",
            description = "Delete all images by base name in filename"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            )),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            ))})
    public ResponseEntity<ResponseMessageDto> deleteImage(
            @Parameter(description = "Dto with name of file ",
                    schema = @Schema(implementation = ImageRequestDto.class))
            @RequestBody ImageRequestDto dto
    ) {
        imageService.deleteImage(dto.getFileName());
        return ResponseEntity.ok(new ResponseMessageDto("Image deleted successfully"));
    }
}
