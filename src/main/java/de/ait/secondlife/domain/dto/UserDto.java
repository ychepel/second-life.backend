package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@Schema(name = "Existing user data", description = "User details")
public class UserDto extends ImageUploadDetails {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User First Name", example = "John")
    private String firstName;

    @Schema(description = "User Last Name", example = "Smith")
    private String lastName;

    @Schema(description = "User Email", example = "user@mail.com")
    private String email;

    @Schema(description = "User creating datetime")
    private LocalDateTime createdAt;

    @Schema(description = "User Location ID", example = "3")
    private Long locationId;

    @Schema(description = "User last login")
    private LocalDateTime lastActive;

    @Schema(description = "List of image's path")
    private ImagePathsResponseDto images;
}
