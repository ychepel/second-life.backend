package de.ait.secondlife.controllers;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.dto.ImageCreationDto;
import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.dto.ImageRequestDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.utilities.EntityUtilities;
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
public class ImageController {

    private final ImageService imageService;
    private final EntityUtilities utilities;

    @PostMapping
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
                            schema = @Schema(implementation = ImagePathsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            )),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseMessageDto.class)
            ))})
    public ResponseEntity<ImagePathsResponseDto> uploadImage(
            @Valid
            @Parameter(description = "Dto with image file, entity type and entity id ", schema = @Schema(implementation = ImageCreationDto.class))
            ImageCreationDto request) {

        String entityType = EntityTypeWithImages.get(request.getEntityType().toLowerCase()).getType();
        Long entityId = request.getEntityId();
        utilities.checkEntityExists(entityType, entityId);
        utilities.checkCredentials(entityType, entityId);
        return ResponseEntity.ok(imageService.saveNewImage(entityType, entityId, request, utilities.getCurrentUserId()));
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
            @Valid
            @Parameter(description = "Dto with base name of file",
                    schema = @Schema(implementation = ImageRequestDto.class))
            @RequestBody ImageRequestDto dto
    ) {
        utilities.checkCredentials(dto.getBaseName());
        imageService.deleteImage(dto.getBaseName());

        return ResponseEntity.ok(new ResponseMessageDto("Image deleted successfully"));
    }
}
