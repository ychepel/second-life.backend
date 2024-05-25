//package de.ait.secondlife.domain.entity;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.UUID;
//
//@Entity
//@Table(name = "category")
//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//@Builder
//public class Category {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
//    private Long id;
//
//    @Column(name = "name")
//    @NotBlank(message = "Category name cannot be empty")
//    @Size(max = 64, message = "Category name cannot be longer than 64 characters")
//    private String name;
//
//    @Column(name = "description")
//    @Size(max = 1000, message = "Category description cannot be longer than 1000 characters")
//    private String description;
//
//    @Column(name="is_active")
//    private Boolean isActive;
//
//
//}
//
