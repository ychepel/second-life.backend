package de.ait.secondlife.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OfferRequestWithPaginationDto {
    private List<OfferRequestDto> offers;
    private int pageNumber;
    private int pageSize;
    public int totalPages;
    private long totalElements;
    private Boolean isFirstPage;
    private Boolean isLastPage;

}


