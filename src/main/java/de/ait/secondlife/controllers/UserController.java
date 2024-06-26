package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.exception_handling.dto.ValidationErrorsDto;
import de.ait.secondlife.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;

@RestController
@RequestMapping("/v1/users")
@AllArgsConstructor
@Tag(name = "User controller", description = "Handles user registration requests")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(
            summary = "Registration",
            description = """
                    Registration new Users in application. Available to all unauthorized users.
                    Password requirements:
                    1. Includes at least one letter (A-Z or a-z).
                    2. Includes at least one digit
                    3. Includes at least one special character from the following set: @,#,$,%,^,&,+,=,!
                    4. No spaces allowed
                    5. Minimum length at least 8 characters"""
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User is successfully created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))
            ),
            @ApiResponse(responseCode = "422",
                    description = "Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))}
    )
    public ResponseEntity<UserDto> register(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User date to register")
            UserCreationDto newUserDto) {
        UserDto userDto = userService.register(newUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @GetMapping("/me")
    @Operation(summary = "Getting information about current user", description = "Available only for User role")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))
            )
    })
    public ResponseEntity<UserDto> getCurrentUser() throws CredentialException {
        return ResponseEntity.ok(userService.getCurrentUser());
    }


    @GetMapping("{id}/set-active")
    public ResponseEntity<UserDto> setActive(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Set active the user according to confirmation code")
            @PathVariable("id") Long userId, @RequestParam("code") String code){
        UserDto userDto = userService.setActive(userId,code);
        return ResponseEntity.ok(userDto);
    }
}
