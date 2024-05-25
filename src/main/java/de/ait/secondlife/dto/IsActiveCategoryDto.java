package de.ait.secondlife.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Define isActive flag of Category")
public class IsActiveCategoryDto {

    @Schema(description = "active or not flag", example = "true")
    private boolean active;
}
