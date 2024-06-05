package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;

import lombok.Data;

@Data
@Schema(description = "Image response dto with properties of image")
public class ImagePropsResponseDto {
    @Column(name = "size")
    private String size;

    @Column(name = "path")
    private String path;

    @Column(name = "base_name")
    private String baseName;

    @Column(name = "extension")
    private String extension;
}
