package de.ait.secondlife.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@Schema(name = "Refresh token")
public class RefreshRequestDto {

    private String refreshToken;
}
