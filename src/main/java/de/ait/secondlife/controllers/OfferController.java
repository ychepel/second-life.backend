package de.ait.secondlife.controllers;

import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.domain.dto.*;

import de.ait.secondlife.exception_handling.dto.ValidationErrorsDto;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.PaginationParameterIsWrongException;
import de.ait.secondlife.services.interfaces.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;

@RestController
@RequestMapping("/v1/offers")
@RequiredArgsConstructor
@Tag(name = "Offer controller", description = "Controller for some operations with available offer")
public class OfferController {

    private final OfferService service;

    private final String PAGE_VALUE = "0";
    private final String SIZE_VALUE = "10";
    private final String SORT_BY = "createdAt";

    @GetMapping("/all")
    @Operation(
            summary = "Get all offers ",
            description = "Receiving all offers available in the database with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseWithPaginationDto.class))),
    })
    public ResponseEntity<OfferResponseWithPaginationDto> getAll(
            @RequestParam(defaultValue = PAGE_VALUE)
            @Parameter(description = "Requested page number. ", example = "0")
            int page,
            @RequestParam(defaultValue = SIZE_VALUE)
            @Parameter(description = "Number of entities per page.", example = "10")
            int size,
            @RequestParam(defaultValue = SORT_BY)
            @Parameter(description = "Sorting field.", examples = {
                    @ExampleObject(name = "Sort by created time", value = "createdAt"),
                    @ExampleObject(name = "Sort by title", value = "title"),
                    @ExampleObject(name = "Sort by start price", value = "startPrice")
            })
            String sortBy,
            @RequestParam(defaultValue = "true")
            @Parameter(description = "Sorting direction.", examples = {
                    @ExampleObject(name = "Sort direction is ascending(default)", value = "true"),
                    @ExampleObject(name = "Sort direction is descending", value = "false")
            })
            Boolean isAsc,
            @RequestParam(required = false)
            @Parameter(description = "Category id for filtration. Can be null." +
                    " Optional parameter", example = "3")
            Long category_id,
            @RequestParam(required = false)
            @Parameter(description = "Offer status for filtration. Can be null." +
                    " Optional parameter", example = "DrAfT")
            String status,
            @RequestParam(required = false)
            @Parameter(description = "Is offer free or not for filtration. Can be null." +
                    " Optional parameter", example = "true, false")
            Boolean free) {

        return ResponseEntity.ok(service.findOffers(
                getPageable(page, size, sortBy, isAsc),
                category_id,
                status,
                free));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get offer by id",
            description = "Receiving offer by id available in the database "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<OfferResponseDto> getById(
            @PathVariable
            @Parameter(description = "Offer id.", example = "123")
            Long id) {
        return ResponseEntity.ok(service.getDto(id));
    }

    @GetMapping("/user/{id}")
    @Operation(
            summary = "Get all offers by user id",
            description = "Receiving all offers by user id available in the database with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseWithPaginationDto.class))),
            @ApiResponse(responseCode = "404", description = "Offers not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<OfferResponseWithPaginationDto> getByUserId(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Requested page number. ", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of entities per page. ", example = "0")
            int size,
            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Sorting field. ", example = "createdAt")
            String sortBy,
            @RequestParam(defaultValue = "true")
            @Parameter(description = "Sorting direction.", examples = {
                    @ExampleObject(name = "Sort direction is ascending(default)", value = "true"),
                    @ExampleObject(name = "Sort direction is descending", value = "false")
            })
            Boolean isAsc,
            @RequestParam(required = false)
            @Parameter(description = "Category id for filtration. Can be null." +
                    " Optional parameter", example = "3")
            Long category_id,
            @RequestParam(required = false)
            @Parameter(description = "Offer status for filtration. Can be null." +
                    " Optional parameter", example = "DrAfT")
            String status,
            @RequestParam(required = false)
            @Parameter(description = "Is offer free or not for filtration. Can be null." +
                    " Optional parameter", example = "true, false")
            Boolean free,
            @PathVariable
            @Parameter(description = "User id in Long format. ", example = "2321")
            Long id) {
        return ResponseEntity.ok(service.findOffersByUserId(
                id,
                getPageable(page, size, sortBy, isAsc),
                category_id,
                status,
                free));
    }

    @PostMapping
    @Operation(
            summary = "Create new offer",
            description = "Creating a new offer and saving it in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Offer created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<OfferResponseDto> create(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Offer create DTO ")
            OfferCreationDto dto) throws CredentialException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOffer(dto));
    }

    @PutMapping
    @Operation(
            summary = "Update offer",
            description = "Updating the existing offer and saving it in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class)))
    })
    public ResponseEntity<OfferResponseDto> update(
            @Valid
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Offer update DTO ")
            OfferUpdateDto dto
    ) throws CredentialException {
        return ResponseEntity.ok(service.updateOffer(dto));
    }

    @PatchMapping("/{id}/reject")
    @Operation(
            summary = "Change offer status to Rejected",
            description = "Rejecting an Offer if the ad does not comply with site policies. Available only for Admin role and for Offers in <i>Verification</i> status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> reject(
            @PathVariable Long id,
            @RequestBody @Valid OfferRejectionDto offerRejectionDto
    ) {
        service.rejectOffer(id, offerRejectionDto);
        return ResponseEntity.ok(
                new ResponseMessageDto(
                        String.format("Status for offer with id <%s> was changed to DRAFT successfully", id)
                )
        );
    }

    @PatchMapping("/{id}/start-auction")
    @Operation(
            summary = "Start auction",
            description = "Transferring an Offer from <i>Verification</i> status to the <i>Auction started</i>. Available only for Admin role and for Offers in <i>Verification</i> status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> startAuction(@PathVariable Long id) {
        service.startAuction(id);
        return ResponseEntity.ok(
                new ResponseMessageDto(
                        String.format("Auction for offer with id <%s> was started successfully", id)
                )
        );
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel offer by owner",
            description = "Disabling an Offer. Available only for Offer owners and for Offers in statuses: <i>Verification, Auction started, Auction finished, Qualification</i>."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> cancel(@PathVariable Long id) {
        service.cancelOffer(id);
        return ResponseEntity.ok(
                new ResponseMessageDto(String.format("Offer with id <%s> was canceled successfully", id))
        );
    }

    @DeleteMapping("/{id}/block-by-admin")
    @Operation(
            summary = "Cancel offer by admin",
            description = "Disabling an Offer. Available only for Admin role and for Offers in statuses: <i>Draft, Verification, Auction started, Auction finished, Qualification</i>."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> block(@PathVariable Long id) {
        service.blockOfferByAdmin(id);
        return ResponseEntity.ok(
                new ResponseMessageDto(String.format("Offer with id <%s> was blocked successfully", id))
        );
    }

    @PatchMapping("/{id}/complete")
    @Operation(
            summary = "Completing offer",
            description = "Indication of the winner's bid and transferring an Offer from <i>Qualification</i> status to Completed. Available only for Offer owners and for Offers in <i>Qualification</i> status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorsDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "Offer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<OfferResponseDto> complete(
            @PathVariable Long id,
            @RequestBody @Valid OfferCompletionDto offerCompletionDto
    ) {
        ;
        return ResponseEntity.ok(service.completeOffer(id, offerCompletionDto));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search offers",
            description = "Searching string <i>pattern</i> among all Offers in status AUCTION_STARTED. The search is carried out using the Offer title and Offer description fields and is not case-sensitive."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful operation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfferResponseWithPaginationDto.class))
            ),
    })
    public ResponseEntity<OfferResponseWithPaginationDto> search(
            @RequestParam
            String pattern,
            @RequestParam(defaultValue = PAGE_VALUE)
            @Parameter(description = "Requested page number. ", example = "0")
            int page,
            @RequestParam(defaultValue = SIZE_VALUE)
            @Parameter(description = "Number of entities per page.", example = "10")
            int size,
            @RequestParam(defaultValue = SORT_BY)
            @Parameter(description = "Sorting field.", examples = {
                    @ExampleObject(name = "Sort by created time", value = "createdAt"),
                    @ExampleObject(name = "Sort by title", value = "title"),
                    @ExampleObject(name = "Sort by start price", value = "startPrice")
            })
            String sortBy,
            @RequestParam(defaultValue = "true")
            @Parameter(description = "Sorting direction.", examples = {
                    @ExampleObject(name = "Sort direction is ascending(default)", value = "true"),
                    @ExampleObject(name = "Sort direction is descending", value = "false")
            })
            Boolean isAsc,
            @RequestParam(required = false, name = "location_id")
            @Parameter(description = "Location id for filtration. Can be null. Optional parameter", example = "3")
            Long locationId
    ){
        return ResponseEntity.ok(service.searchOffers(getPageable(page, size, sortBy, isAsc), locationId, pattern));
    }

    private Pageable getPageable(int page, int size, String sortBy, Boolean isAsc) {
        try {
            Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
           var res = PageRequest.of(page, size, Sort.by(direction, sortBy));
            return res;
        } catch (IllegalArgumentException e) {
            throw new PaginationParameterIsWrongException(page, size, sortBy);
        }
    }
}
