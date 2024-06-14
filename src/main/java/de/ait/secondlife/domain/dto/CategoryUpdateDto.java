package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "Category update dto", description = "Description of the category")
public class CategoryUpdateDto extends BaseNameOfImages {

    @Schema(description = "category name", example = "Electronics and gadgets")
    private String name;

    @Schema(description = "detailed description of the category", example = "Smartphones,Laptops,Televisions,Peripherals")
    @Size(max = 1000)
    private String description;
}
