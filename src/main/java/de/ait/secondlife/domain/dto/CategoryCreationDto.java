package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Schema(name = "New Category to save into DB")
public class CategoryCreationDto  extends BaseNameOfImages {

    @Schema(description = "category name", example = "Electronics and gadgets")
    @NotBlank(message = "Name must not be blank")
    private String name;

    @Schema(description = "detailed description of the category", example = "Smartphones,Laptops,Televisions,Peripherals")
    @Size(max = 1000)
    private String description;
}
