package de.ait.secondlife.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "List of offer response DTO with pagination")
public class OfferResponseWithPaginationDto {
    @Schema(description = "Set of offer response DTO")
    private Set<OfferResponseDto> offers;
    @Schema(description = "Current page number", example = "6")
    private int pageNumber;
    @Schema(description = "Current page size", example = "20")
    private int pageSize;
    @Schema(description = "Total number of pages", example = "134")
    public int totalPages;
    @Schema(description = "Total number of elements", example = "345")
    private long totalElements;
    @Schema(description = "Is fist page?", example = "true")
    private Boolean isFirstPage;
    @Schema(description = "Is last page?", example = "true")
    private Boolean isLastPage;

}


