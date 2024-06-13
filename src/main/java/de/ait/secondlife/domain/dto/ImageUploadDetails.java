package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public abstract class ImageUploadDetails {
    @Schema(description = "Some information about upload image. Сan be empty"
            , example = "Images with base name <d3f1a2b3-c456-789d-012e-3456789abcde, a1b2c3d4-e5f6-7890-1234-56789abcdef0, " +
            "0fedcba9-8765-4321-0fed-cba987654321> were not uploaded as they had been used previously")
    private String  imageUploadInfo;

    @Schema(description = "List of image's path")
    private  ImagePathsResponseDto images;
}
