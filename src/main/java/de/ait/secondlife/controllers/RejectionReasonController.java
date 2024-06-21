package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.RejectionReasonsDto;
import de.ait.secondlife.services.interfaces.RejectionReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rejection-reasons")
@Tag(name = "Rejection reason controller", description = "Endpoint for retrieving Rejection reasons for Offer verification process")
public class RejectionReasonController {

    private final RejectionReasonService service;

    @GetMapping
    @Operation(summary = "Get list of Rejection reasons", description = "Available to all users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RejectionReasonsDto.class))),
    })
    public ResponseEntity<RejectionReasonsDto> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
