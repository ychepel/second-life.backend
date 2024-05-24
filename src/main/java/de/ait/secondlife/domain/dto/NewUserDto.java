package de.ait.secondlife.domain.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class NewUserDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
