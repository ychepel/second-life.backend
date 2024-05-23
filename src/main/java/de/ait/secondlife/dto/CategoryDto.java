package de.ait.secondlife.dto;

import lombok.*;

import java.util.Objects;

@Data
@ToString
public class CategoryDto {

    private Long id;

    private String name;

    private String description;

    private boolean isActive;
}
