package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(description = "Image request dto")
@AllArgsConstructor
public class ImageRequestDto {

    //TODO: PR review - remake logic on processing base_name instead of full path
    @Schema(
            description = "Full name of image file",
            example = "https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\1\\1024x1024_91dcf1db-b934-479a-828f-59b46a522a5a.jpg"
    )
    private String fileName;
}
