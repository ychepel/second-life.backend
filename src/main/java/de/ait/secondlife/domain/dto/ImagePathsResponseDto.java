package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Image's path dto")
@AllArgsConstructor
public class ImagePathsResponseDto {

    @Schema(description = "Code of type of entity",
            example = """
                    {
                        "47424034-00e8-4358-b352-e16023279883": {
                            "1024x1024": "https://domain.com/prod/offer/1/1024x1024_47424034-00e8-4358-b352-e16023279883.jpg",
                    		"320x320": "https://domain.com/prod/offer/1/320x320_47424034-00e8-4358-b352-e16023279883.jpg",
                            "64x64": "https://domain.com/prod/offer/1/64x64_47424034-00e8-4358-b352-e16023279883.jpg"
                        },
                    	"a1b2c3d4-e5f6-7890-1234-56789abcdef0": {
                            "1024x1024": "https://domain.com/prod/offer/1/1024x1024_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg",
                    		"320x320": "https://domain.com/prod/offer/1/320x320_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg",
                            "64x64": "https://domain.com/prod/offer/1/64x64_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg"
                        }
                    }
                    """)
    private Map<String, Map<String, String>> values;
}
