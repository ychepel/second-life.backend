package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@Schema(description = "Offer update DTO")
public class OfferUpdateDto extends BaseNameOfImages {

    @Schema(description = "Offer id "
            , example = "123")
    @NotNull(message = "Offer id cannot be null")
    private Long id;

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

    @Schema(description = "Starting offer price", example = "123")
    private BigDecimal startPrice;

    @Schema(description = "Possible buyout price without bidding", example = "222")
    private BigDecimal winBid;

    @Schema(description = "Offer is free or not", example = "false")
    private Boolean isFree;

    @Schema(description = "Category id", example = "2")
    private Long categoryId;

    @Schema(description = "Location id", example = "1")
    private Long locationId;

    @Schema(description = "List of base names of picture files that have been previously created",
            example = "[\"d3f1a2b3-c456-789d-012e-3456789abcde\", \"a1b2c3d4-e5f6-7890-1234-56789abcdef0\", \"0fedcba9-8765-4321-0fed-cba987654321\"]")
    private Set<String> baseNameOfImages;

    @Schema(description = "Requirement for Admin verification ", example = "true")
    private Boolean sendToVerification;
}




