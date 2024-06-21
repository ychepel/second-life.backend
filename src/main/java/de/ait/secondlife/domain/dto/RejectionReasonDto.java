package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Rejection reason DTO")
public class RejectionReasonDto {

    @Schema(description = "Rejection reason ID", example = "11")
    private Long id;

    @Schema(description = "Rejection reason", example = "Incorrect category")
    private String name;
}
