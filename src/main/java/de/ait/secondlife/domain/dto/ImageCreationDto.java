package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Image create dto")
public class ImageCreationDto {

    @Schema(description = "Type of entity", allowableValues = {"offer", "user", "category"}, example = "offer")
    @NotNull(message = "Entity type cannot be null")
    private String entityType;

    @Schema(description = "Id of entity." +
            " Can be null if the entity has not yet been " +
            "created during pre-registration", examples = {"34", "null"})
    private Long entityId;

    @Schema(description = "File with image")
    @NotNull(message = "Image file cannot be null")
    private MultipartFile file;
}
