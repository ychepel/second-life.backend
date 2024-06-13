package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Image request dto")
@AllArgsConstructor
@NoArgsConstructor
public class ImageRequestDto {
    @Schema(
            description = "Base name of image file",
            example = "91dcf1db-b934-479a-828f-59b46a522a5a"
    )
    @NotBlank(message = "Base name cannot blank")
    private String baseName;
}
