package de.ait.secondlife.domain.entity;

import de.ait.secondlife.domain.constants.OfferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "status")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name")
    @NotBlank(message = "Status name cannot be empty")
    private OfferStatus name;
}
