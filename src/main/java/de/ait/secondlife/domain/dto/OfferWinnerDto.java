package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Schema(description = "Offer winner DTO")
@NoArgsConstructor
public class OfferWinnerDto {

    @Schema(description = "Winner bid id", example = "123")
    private Long bidId;

    @Schema(description = "Shortened username", example = "John S.")
    private String nameShorted;

    @Schema(description = "User Email", example = "user@mail.com")
    private String email;

    @Schema(description = "Bid value", example = "123")
    private BigDecimal bidValue;
}
