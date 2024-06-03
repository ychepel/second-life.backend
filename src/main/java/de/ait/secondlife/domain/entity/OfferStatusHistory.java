package de.ait.secondlife.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer_status_history")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OfferStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="created_at")
    @NotNull(message="Created at cannot be null" )
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    @NotNull(message="Offer cannot be null" )
    private Offer offer ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    @NotNull(message="Status cannot be null" )
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejection_id")
    private RejectionReasons rejection;
}


