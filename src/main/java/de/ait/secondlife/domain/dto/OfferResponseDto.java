package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Offer response DTO")
public class OfferResponseDto extends ImageUploadDetails {

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

    @Schema(description = "Date and time of the start of the auction", example = "2024-05-27T17:11:18.149566")
    private LocalDateTime auctionStartAt;

    @Schema(description = "Date and time of the end of the auction", example = "2024-05-30T17:11:18.149566")
    private LocalDateTime auctionEndAt;

    @Schema(description = "Starting offer price", example = "123")
    private BigDecimal startPrice;

    @Schema(description = "Possible buyout price without bidding", example = "322")
    private BigDecimal winBid;

    @Schema(description = "Offer is free or not", example = "false")
    private Boolean isFree;

    @Schema(description = "User id of the owner of the offer", example = "34")
    private Long ownerId;

    @Schema(description = "Full name of the offer owner", example = "John Smith")
    private String ownerFullName;

    @Schema(description = "Current status", example = "VERIFICATION")
    private String status;

    @Schema(description = "Category id", example = "2")
    private Long categoryId;

    @Schema(description = "Winner bid id", example = "123")
    private Long winnerBidId;

    @Schema(description = "Location id", example = "1")
    private Long locationId;

    @Schema(description = "Auction duration in days", example = "3")
    private Integer auctionDurationDays;

    @Schema(description = "Maximum bid value for auction", example = "47")
    private BigDecimal maxBidValue;

    @Schema(description = "Quantity of auction bids", example = "8")
    private int bidsCount;

    @Schema(description = "Is the current user a participant in the auction", example = "false")
    private Boolean isCurrentUserAuctionParticipant;
}
