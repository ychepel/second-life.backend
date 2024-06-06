package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Image create dto")
public class ImageCreateDto {
    @Parameter(description = "Code of type of entity",
            examples = {
                    @ExampleObject(name = "offer", value = "1"),
                    @ExampleObject(name = "user", value = "2"),
                    @ExampleObject(name = "category", value = "3")
            })
    @NotNull
    private int entityCode;
    @Schema(description = "Id of entity", example ="324")
    @NotNull
    private Long entityId;
    @Schema(description = "File with image")
    private MultipartFile file;
}
