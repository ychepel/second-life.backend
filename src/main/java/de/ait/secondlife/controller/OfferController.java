package de.ait.secondlife.controller;


import de.ait.secondlife.domain.Response;
import de.ait.secondlife.domain.dto.OfferCreationDto;
import de.ait.secondlife.domain.dto.OfferRequestDto;
import de.ait.secondlife.domain.dto.OfferRequestWithPaginationDto;
import de.ait.secondlife.domain.dto.OfferUpdateDto;
import de.ait.secondlife.exceptionHandler.exeptions.PaginationParameterIsWrongException;
import de.ait.secondlife.service.interfaces.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/offer")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService service;

    @GetMapping("/all")
    public ResponseEntity<OfferRequestWithPaginationDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Pageable pageable;
        try {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } catch (IllegalArgumentException e) {
            throw new PaginationParameterIsWrongException(page, size, sortBy);
        }

        return ResponseEntity.ok(service.findOffers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferRequestDto> getById(@PathVariable UUID id) {

        return ResponseEntity.ok(service.findOfferById(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<OfferRequestWithPaginationDto> getByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @PathVariable Long id) {
        Pageable pageable;
        try {
            pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        } catch (IllegalArgumentException e) {
            throw new PaginationParameterIsWrongException(page, size, sortBy);
        }
        return ResponseEntity.ok(service.findOffersByUserId(id,pageable));
    }

    @PostMapping
    public ResponseEntity<OfferRequestDto> create(@RequestBody OfferCreationDto dto) {

        return ResponseEntity.ok(service.createOffer(dto));
    }

    @PutMapping
    public ResponseEntity<Response> update(@RequestBody OfferUpdateDto dto) {

        service.updateOffer(dto);
        return ResponseEntity.ok(
                new Response(String.format("Offer with id <%s> updated successful", dto.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> remove(@PathVariable UUID id) {
        service.removeOffer(id);
        return ResponseEntity.ok(
                new Response(String.format("Offer with id <%s> removed successful", id)));
    }

    @PutMapping("/recover/{id}")
    public ResponseEntity<Response> recover(@PathVariable UUID id) {
        service.recoverOffer(id);
        return ResponseEntity.ok(
                new Response(String.format("Offer with id <%s> recovered successful", id)));
    }
}
