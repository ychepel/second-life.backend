package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "Category", description = "Description of the category")
public class CategoryDto extends ImageUploadDetails {

    @Schema(description = "category id", example = "1")
    private Long id;

    @Schema(description = "category name", example = "Electronics and gadgets")
    private String name;

    @Schema(description = "detailed description of the category", example = "Smartphones,Laptops,Televisions,Peripherals")
    @Size(max = 1000)
    private String description;

    @Schema(description = "active or not flag", example = "true")
    private boolean active;
}
