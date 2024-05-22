package de.ait.secondlife.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OfferUpdateDto {
    private UUID id;
    private String title;
    private String description;
    private Integer auctionDurationDays;
    private BigDecimal startPrice;
    private BigDecimal step;
    private BigDecimal winBid;
    private Boolean isFree;
    private Long categoryId;
}

