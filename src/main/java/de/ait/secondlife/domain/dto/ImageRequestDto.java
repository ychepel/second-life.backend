package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Image request dto")
public class ImageRequestDto {
    @Schema(description = "Full name of image file", example = "prod/offer/1/1024x1024_91dcf1db-b934-479a-828f-59b46a522a5a.jpg")
    private String fileName;
}
