package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Bid create DTO")
public class BidCreationDto {

    @Schema(description = "Offer ID", example = "56")
    @NotNull(message = "Offer ID cannot be null")
    private Long offerId;

    @Schema(description = "Bid value", example = "123")
    @NotNull(message = "Bid value cannot be null")
    private BigDecimal bidValue;
}


