package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Bids response DTO")
public class BidsResponseDto {

    @Schema(description = "List of auction bids")
    List<BidResponseDto> bids;
}
