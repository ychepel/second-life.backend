package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class ImageUploadDetails {

    @Schema(description = "List of image's path")
    private  ImagePathsResponseDto images;
}
