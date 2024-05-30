package de.ait.secondlife.exception_handling.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "Validation Error", description = "Validation error description")
public class ValidationErrorDto {

    @Schema(description = "name of the field in which the error occurred", example = "some field name")
    private String field;

    @Schema(description = "the value entered by the user and which was rejected by the server", example = "some value of the field")
    private String rejectedValue;

    @Schema(description = "the message we need to show to the user", example = "error description")
    private String message;
}