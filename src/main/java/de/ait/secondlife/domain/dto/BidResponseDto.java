package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Bid response DTO")
public class BidResponseDto {

    @Schema(description = "Bid ID", example = "11")
    private Long id;

    @Schema(description = "Bid value", example = "123")
    private BigDecimal bidValue;

    @Schema(description = "Bid creating datetime")
    private LocalDateTime createdAt;

    @Schema(description = "Offer ID", example = "123")
    private Long offerId;

    @Schema(description = "User ID", example = "123")
    private Long userId;

    @Schema(description = "Shortened username", example = "John S.")
    private String userNameShorted;

    @Schema(description = "User Email", example = "user@mail.com")
    private String userEmail;
}
