package de.ait.secondlife.domain.dto;

import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.domain.entity.Category;
import de.ait.secondlife.domain.entity.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class OfferCreationDto {
    private String title;
    private String description;
    private Integer auctionDurationDays;
    private BigDecimal startPrice;
    private BigDecimal step;
    private BigDecimal winBid;
    private Boolean isFree;
    private Long userId;
    private Long categoryId;
}


