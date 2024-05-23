package de.ait.secondlife.security.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class TokenResponseDto {

    private String accessToken;
    private String refreshToken;
}
