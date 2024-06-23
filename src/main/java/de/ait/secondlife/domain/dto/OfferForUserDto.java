package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "Current user details")
public class OfferForUserDto {

    @Schema(description = "Is the current user a participant in the auction", example = "false")
    private Boolean isAuctionParticipant;

    @Schema(description = "Max bid value of current user", example = "123")
    private BigDecimal maxBidValue;
}
