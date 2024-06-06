package de.ait.secondlife.controllers;


import de.ait.secondlife.constants.EntityType;
import de.ait.secondlife.domain.dto.ImageCreateDto;

import de.ait.secondlife.domain.dto.ImagePathsResponseDto;
import de.ait.secondlife.domain.dto.ImageRequestDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.services.interfaces.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Set;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
@Tag(name = "Image controller", description = "Controller for some operations with images")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        )),
        @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        )),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        ))
})
public class ImageContorller {

    private final ImageService imageService;

    @PostMapping("/upload")
    @Operation(
            summary = "Create new image",
            description = "Create new image for entity by entity id "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),})
    public ResponseEntity<ResponseMessageDto> uploadImage(
            @Valid
            @Parameter(description = "Dto with image file, entity type and entity id ", schema = @Schema(implementation = ImageCreateDto.class))
            ImageCreateDto request) {
        imageService.saveNewImage(request);
        return ResponseEntity.ok(
                new ResponseMessageDto("Image(s) successful saved"));
    }

    @GetMapping
    @Operation(
            summary = "Get names of entity images",
            description = "Get all names of image of entity by type and id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json"
                            , schema = @Schema(implementation = ImagePathsResponseDto.class)
                    ))})
    public ResponseEntity<ImagePathsResponseDto> getImages(
            @RequestParam
            @Parameter(description = "Code of type of entity", examples = {
                    @ExampleObject(name = "offer", value = "1"),
                    @ExampleObject(name = "user", value = "2"),
                    @ExampleObject(name = "category", value = "3")
            })
            int typeCode,
            @RequestParam
            @Parameter(description = "Id of entity", example = "343")
            Long id) {
        String entityType = EntityType.get(typeCode).getType();
        return ResponseEntity.ok(imageService.findAllImageForEntity(entityType, id));
    }

  @PostMapping
  @Operation(
          summary = "Get image",
          description = "Get image by filename"
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successful operation",
                  content = @Content(mediaType = "application/json"
                          , schema = @Schema(implementation = StreamingResponseBody.class)
                  ))})
    public ResponseEntity<StreamingResponseBody> getImageByFile(
            @RequestBody ImageRequestDto dto
  ){
      InputStream inputStream = imageService.getImage(dto.getFileName());
      return ResponseEntity.ok(inputStream::transferTo);
  }

}
