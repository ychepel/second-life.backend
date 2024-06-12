package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Image create dto")
public class ImageCreationDto {

    @Parameter(description = "Type of entity",
            examples = {
                    @ExampleObject(value = "offer"),
                    @ExampleObject(value = "user"),
                    @ExampleObject(value = "category")
            })
    @NotNull
    private String entityType;

    @Schema(description = "Id of entity", example = "324")
    @NotNull //TODO: PR review - remove @NotNull to allow creating temporary images before entity creation
    private Long entityId;

    @Schema(description = "File with image")
    //TODO: PR review - add @NotNull
    private MultipartFile file;
}
