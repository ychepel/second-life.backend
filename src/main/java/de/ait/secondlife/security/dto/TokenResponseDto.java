package de.ait.secondlife.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Schema(name = "JSON Web Tokens")
public class TokenResponseDto {

    @Schema(description = "User or Admin ID", example = "123")
    private Long clientId;

    private String accessToken;
    private String refreshToken;
}
