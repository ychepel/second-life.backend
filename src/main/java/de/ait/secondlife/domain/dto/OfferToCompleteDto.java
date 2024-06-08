package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OfferToCompleteDto {

    @NotNull(message = "Offer ID cannot be null")
    @Schema(description = "Offer iD", example = "123")
    private Long id;

    @NotNull(message = "Winner Bid ID cannot be null")
    @Schema(description = "Winner Bid ID", example = "123")
    private Long winnerBidId;
}
