package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Offer update DTO")
public class OfferUpdateDto {

    @Schema(description = "Offer id "
            , example = "898449f7-e9d1-4d00-9fd6-cae203452f3a")
    @NotNull(message = "Offer id cannot be null")
    private UUID id;

    @Schema(description = "Title of a offer", example = "Upholstered chair")
    @Size(min = 5, max = 64, message = "Title must be between 5 and 64 characters")
    private String title;

    @Schema(description = "Description of a offer",
            example = "In a small village, a curious cat named Whiskers explored every nook " +
                    "and cranny. One day, Whiskers discovered a hidden garden filled with vibrant " +
                    "flowers and buzzing bees. The villagers often saw Whiskers napping among the " +
                    "blossoms, bringing a smile to everyone's face. Whiskers " +
                    "became the beloved guardian of the magical garden.\n")
    private String description;

    @Schema(description = "Auction duration in days", example = "3")
    private Integer auctionDurationDays;

    @Schema(description = "Starting offer price", example = "1234.34")
    private BigDecimal startPrice;

    @Schema(description = "Bidding step", example = "12.34")
    private BigDecimal step;

    @Schema(description = "Possible buyout price without bidding", example = "1222.34")
    private BigDecimal winBid;

    @Schema(description = "Offer is free or not", example = "true")
    private Boolean isFree;

    @Schema(description = "Category id", example = "22")
    private Long categoryId;
}




