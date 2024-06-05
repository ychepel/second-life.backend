package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Image create dto")
public class ImageCreateDto {
    @Schema(description = "Type of entity", examples = {"offer", "user", "category"})
    @NotBlank
    String entityType;
    @Schema(description = "Id of entity", example ="324")
    @NotNull
    Long entityId;
    @Schema(description = "File with image")
    MultipartFile file;
}
