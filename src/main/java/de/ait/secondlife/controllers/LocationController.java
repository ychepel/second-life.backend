package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.CategoryDto;
import de.ait.secondlife.domain.dto.LocationDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.services.interfaces.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/locations")
@Tag(name = "Location controller", description = "Allow to get location by id or to get the list of all locations")
public class LocationController {

    private final LocationService service;

    @Operation(summary = "Get location by id", description = "Accessible to all users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))}),
            @ApiResponse(responseCode = "404",
                    description = "Resource not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @Operation(summary = "Get all locations", description = "Accessible to all users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class))})
    })
    @GetMapping()
    public ResponseEntity<List<LocationDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
