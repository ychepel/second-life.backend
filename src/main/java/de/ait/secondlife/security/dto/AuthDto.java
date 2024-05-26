package de.ait.secondlife.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "Authentication data", description = "User or admin credentials")
public class AuthDto {

    @Schema(description = "User/Admin Email", example = "example@mail.com")
    private String email;

    @Schema(description = "User/Admin password", example = "Qwerty!123")
    private String password;
}
