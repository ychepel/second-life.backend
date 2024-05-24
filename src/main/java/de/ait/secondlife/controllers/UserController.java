package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.NewUserDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@AllArgsConstructor
@Tag(name = "User controller", description = "Handles user registration requests")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Registration",
            description = "Registration new Users in application. Available to all unauthorized users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)}
    )
    public ResponseEntity<UserDto> register(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User DTO to register")
            NewUserDto newUserDto) {
        UserDto userDto = userService.register(newUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }
}
