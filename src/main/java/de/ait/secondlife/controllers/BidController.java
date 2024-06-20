package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.BidCreationDto;
import de.ait.secondlife.domain.dto.BidsResponseDto;
import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.exception_handling.dto.ValidationErrorsDto;
import de.ait.secondlife.services.interfaces.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/bids")
@Tag(name = "Bid controller", description = "Handle operations with offer's bids")
public class BidController {

    private final BidService bidService;

    @PostMapping
    @Operation(summary = "Add bid", description = "Saving the bid for the offer in the AUCTION_STARTED status. Available only for the role User, who is not the owner of the offer.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bid created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable Entity",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<ResponseMessageDto> add(@Valid @RequestBody BidCreationDto dto) throws CredentialException {
        bidService.save(dto);
        return ResponseEntity.ok(new ResponseMessageDto("Bid was added successfully"));
    }

    @GetMapping("/offer/{id}")
    @Operation(
            summary = "Get all bids by Offer ID",
            description = "Receiving all bids for auction. Available for Offer owner only.<br>Bids are returned in a list sorted from newest to oldest."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BidsResponseDto.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized for non-owners",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<BidsResponseDto> getByOfferId(
            @PathVariable
            @Parameter(description = "Offer ID", example = "231")
            Long id) throws CredentialException {
        return ResponseEntity.ok(bidService.findAllByOfferId(id));
    }

}
