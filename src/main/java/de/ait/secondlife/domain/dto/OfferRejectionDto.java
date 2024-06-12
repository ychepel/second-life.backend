package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OfferRejectionDto {

    @NotNull(message = "Rejection reason ID cannot be null")
    @Schema(description = "Rejection reason ID", example = "123")
    private Long rejectionReasonId;
}
