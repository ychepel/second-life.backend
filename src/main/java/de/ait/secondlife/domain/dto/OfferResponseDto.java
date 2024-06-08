package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Offer response DTO")
public class OfferResponseDto {

    @Schema(description = "Offer id"
            , example = "123",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Title of a offer", example = "Upholstered chair")
    private String title;

    @Schema(description = "Description of a offer",
            example = "In a small village, a curious cat named Whiskers explored every nook " +
                    "and cranny. One day, Whiskers discovered a hidden garden filled with vibrant " +
                    "flowers and buzzing bees. The villagers often saw Whiskers napping among the " +
                    "blossoms, bringing a smile to everyone's face. Whiskers " +
                    "became the beloved guardian of the magical garden.\n")
    private String description;

    @Schema(description = "Date and time of the end of the auction", example = "2024-05-30T17:11:18.149566")
    private LocalDateTime endAt;

    @Schema(description = "Starting offer price", example = "1234.34")
    private BigDecimal startPrice;

    @Schema(description = "Bidding step", example = "12.34")
    private BigDecimal step;

    @Schema(description = "Possible buyout price without bidding", example = "1222.34")
    private BigDecimal winBid;

    @Schema(description = "Offer is free or not", example = "true")
    private Boolean isFree;

    @Schema(description = "User id of the owner of the offer", example = "34")
    private Long ownerId;

    @Schema(description = "Status id", example = "26")
    private Long statusId;

    @Schema(description = "Category id", example = "22")
    private Long categoryId;

    @Schema(description = "Winner bid id", example = "123")
    private Long winnerBidId;

    @Schema(description = "Location id", example = "1")
    private Long locationId;
}
