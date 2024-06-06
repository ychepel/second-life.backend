package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Image's path dto")
@AllArgsConstructor
public class ImagePathsResponseDto {
    @Parameter(description = "Code of type of entity",
            examples = {
                    @ExampleObject(name = "1024x1024", value = "prod/offer/1/1024x1024_91dcf1db-b934-479a-828f-59b46a522a5a.jpg"),
                    @ExampleObject(name = "320x320", value = "prod/offer/1/320x320_91dcf1db-b934-479a-828f-59b46a522a5a.jpg"),
                    @ExampleObject(name = "64x64", value = "prod/offer/1/64x64_91dcf1db-b934-479a-828f-59b46a522a5a.jpg")
            })
    private Map<String,String> images;
}
