package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Rejection reasons DTO")
public class RejectionReasonsDto {

    @Schema(description = "List of rejection reasons")
    List<RejectionReasonDto> reasons;
}
