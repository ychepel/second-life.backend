package de.ait.secondlife.security.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class RefreshRequestDto {

    private String refreshToken;
}
