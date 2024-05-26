package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Schema(name = "New user data", description = "Data for registration new user")
public class NewUserDto {

    @NotBlank(message = "First Name cannot be empty")
    @Schema(description = "User First Name", example = "John")
    private String firstName;

    @NotBlank(message = "Last Name cannot be empty")
    @Schema(description = "User Last Name", example = "Smith")
    private String lastName;

    @Email(
            message = "Email is not valid",
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @NotNull(message = "Email cannot be null")
    @Schema(description = "User Email", example = "user@mail.com")
    private String email;

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password should include at least one letter (A-Z or a-z), one digit (0-9), one special character (@, #, $, %, ^, &, +, =, !), have no spaces, and be at least 8 characters long"
    )
    @NotNull(message = "Password cannot be empty")
    @Schema(description = "User password", example = "Qwerty!123")
    private String password;
}
