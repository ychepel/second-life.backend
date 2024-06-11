package de.ait.secondlife.domain.entity;

import de.ait.secondlife.domain.interfaces.EntityWithImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
public class Category implements EntityWithImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active",nullable = false)
    private boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    private List<Offer> offers;
}
