package de.ait.secondlife.controllers;


import de.ait.secondlife.domain.dto.ResponseMessageDto;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferResponseDto;
import de.ait.secondlife.domain.dto.OfferResponseWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.exception_handling.OfferExceptionHandler;
import de.ait.secondlife.exception_handling.exeptions.PaginationParameterIsWrongException;
import de.ait.secondlife.services.interfaces.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/offer")
@RequiredArgsConstructor
@Tag(name = "Offer controller", description = "Controller for some operations with available offer")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        )),
        @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        )),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResponseMessageDto.class)
        ))
})
public class OfferController implements OfferExceptionHandler {

    private final OfferService service;

    @GetMapping("/all")
    @Operation(
            summary = "Get all offer ",
            description = "Receiving all offer available in the database with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OfferResponseWithPaginationDto.class))),
    })
    public ResponseEntity<OfferResponseWithPaginationDto> getAll(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Requested page number. ", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of entities per page. ", example = "0")
            int size,
            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Scoring field. ", example = "createdAt")
            String sortBy) {

        Pageable pageable;
        try {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } catch (IllegalArgumentException e) {
            throw new PaginationParameterIsWrongException(page, size, sortBy);
        }

        return ResponseEntity.ok(service.findOffers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get  offer by id",
            description = "Receiving  offer by id available in the database "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OfferResponseDto.class))),
    })
    public ResponseEntity<OfferResponseDto> getById(
            @PathVariable
            @Parameter(description = "Offer id in UUID format.", example = "898449f7-e9d1-4d00-9fd6-cae203452f3a")
            UUID id) {

        return ResponseEntity.ok(service.findOfferById(id));
    }


    @GetMapping("/user/{id}")
    @Operation(
            summary = "Get all offer by user id",
            description = "Receiving  all offer by user id available in the database with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OfferResponseWithPaginationDto.class))),
    })
    public ResponseEntity<OfferResponseWithPaginationDto> getByUserId(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Requested page number. ", example = "0")
            int page,
            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of entities per page. ", example = "0")
            int size,
            @RequestParam(defaultValue = "createdAt")
            @Parameter(description = "Scoring field. ", example = "createdAt")
            String sortBy,
            @PathVariable
            @Parameter(description = "User id in Long format. ", example = "2321")
            Long id) {

        Pageable pageable;
        try {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } catch (IllegalArgumentException e) {
            throw new PaginationParameterIsWrongException(page, size, sortBy);
        }
        return ResponseEntity.ok(service.findOffersByUserId(id, pageable));
    }


    @PostMapping
    @Operation(
            summary = "Create new offer",
            description = "Creating a new offer and saving it in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entity created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OfferResponseDto.class))),
    })
    public ResponseEntity<OfferResponseDto> create(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Offer create DTO ")
            OfferCreationDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOffer(dto));
    }


    @PutMapping
    @Operation(
            summary = "Update  offer",
            description = "Updating the existing offer  and saving it in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> update(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Offer update DTO ")
            OfferUpdateDto dto) {

        service.updateOffer(dto);
        return ResponseEntity.ok(
                new ResponseMessageDto(String.format("Offer with id <%s> updated successful", dto.getId())));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deactivate offer by id",
            description = "Deactivating  the existing offer  by id. This offer won't be available when searching the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> remove(
            @PathVariable
            @Parameter(description = "Offer id in UUID format.", example = "898449f7-e9d1-4d00-9fd6-cae203452f3a")
            UUID id) {
        service.removeOffer(id);
        return ResponseEntity.ok(
                new ResponseMessageDto(String.format("Offer with id <%s> removed successful", id)));
    }

    @PutMapping("/recover/{id}")
    @Operation(
            summary = "Activate offer by id",
            description = "Activating  the existing offer  by id. This offer will be available when searching the database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseMessageDto.class))),
    })
    public ResponseEntity<ResponseMessageDto> recover(
            @PathVariable
            @Parameter(description = "Offer id in UUID format.", example = "898449f7-e9d1-4d00-9fd6-cae203452f3a")
            UUID id) {
        service.recoverOffer(id);
        return ResponseEntity.ok(
                new ResponseMessageDto(String.format("Offer with id <%s> recovered successful", id)));
    }
}
