package de.ait.secondlife.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "offer")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    @NotBlank(message = "Offer title cannot be empty")
    @Size(min=5, max = 64, message = "Title must be between 5  and 64 characters")
    private String title;

    @Column(name = "description")
    @Size(max = 1000, message = "Description cannot be longer than 1000 characters")
    private String description;

    @Column(name = "created_at")
    @NotNull(message = "Created at cannot be null")
    private LocalDateTime createdAt;

    @Column(name = "auction_duration_days")
    @NotNull(message = "Auction duration days at cannot be null")
    private Integer auctionDurationDays;

    @Column(name = "start_price")
    private BigDecimal startPrice;

    @Column(name = "step")
    private BigDecimal step;

    @Column(name = "win_bid")
    private BigDecimal winBid;

    @Column(name = "is_free")
    @NotNull(message = "Is free cannot be null")
    private Boolean isFree;

    @Column(name = "is_active")
    private Boolean isActive;

    //TODO -add offer to user dependence
    @Column(name = "user_id")
    @NotNull(message = "User Id cannot be null")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    @NotNull(message = "Offer status cannot be null")
    private Status status;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id")
//    @NotNull(message="Offer category cannot be null" )
//    private Category category;
//TODO -add offer to category dependence
    @Column(name = "category_id")
    @NotNull(message = "Category Id cannot be null")
    private Long categoryId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_bid_id")
    private Bid winnerBid;
}



