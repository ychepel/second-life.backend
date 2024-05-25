package de.ait.secondlife.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OfferRequestDto {

    private UUID id;
    private String title;
    private String description;
    private LocalDateTime endAt;
    private BigDecimal startPrice;
    private BigDecimal step;
    private BigDecimal winBid;
    private Boolean isFree;
    private Long ownerId;
    private Long statusId;
    private Long categoryId;
    private  UUID winnerBidId;

}
