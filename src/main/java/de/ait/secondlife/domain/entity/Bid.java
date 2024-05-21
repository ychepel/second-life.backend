package de.ait.secondlife.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "bid")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID id;

    @Column(name = "bid_value")
    @NotNull(message = "Bid value cannot be null")
    private BigDecimal bidValue;

    @Column(name = "created_at")
    @NotNull(message = "Created at cannot be null")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    @NotNull(message="Offer cannot be null" )
    private Offer offer ;

    //TODO -add bid to user dependence
    @Column(name = "user_id")
    @NotNull(message="User Id cannot be null" )
    private Long userId;



}

