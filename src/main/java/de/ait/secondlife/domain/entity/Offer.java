package de.ait.secondlife.domain.entity;

import de.ait.secondlife.domain.interfaces.EntityWithImage;
import de.ait.secondlife.constants.OfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "offer")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Offer implements EntityWithImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "auction_duration_days")
    private Integer auctionDurationDays;

    @Column(name = "auction_finished_at")
    private LocalDateTime auctionFinishedAt;

    @Column(name = "start_price")
    private BigDecimal startPrice;

    @Column(name = "win_bid")
    private BigDecimal winBid;

    @Column(name = "is_free")
    private Boolean isFree;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_bid_id")
    private Bid winnerBid;

    @OneToMany(mappedBy = "offer", fetch = FetchType.LAZY)
    private List<Bid> bids;

    public OfferStatus getOfferStatus() {
        return status.getName();
    }

    public BigDecimal getMaxBidValue() {
        if (bids == null) {
            return null;
        }
        return bids.stream()
                .max(Comparator.comparing(Bid::getBidValue))
                .map(Bid::getBidValue)
                .orElse(null);
    }

    public int getBidsCount() {
        return bids == null ? 0 : bids.size();
    }
}



