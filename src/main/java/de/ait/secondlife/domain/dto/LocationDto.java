package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Schema(name = "Location")
public class LocationDto {

    @Schema(description = "location id", example = "1")
    private Long id;

    @Schema(description = "location name", example = "Hessen")
    private String name;
}
