package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
public abstract class BaseNameOfImages {

    @Schema(description = "List of base names of picture files that have been previously created",
            example = "[\"d3f1a2b3-c456-789d-012e-3456789abcde\", \"a1b2c3d4-e5f6-7890-1234-56789abcdef0\", \"0fedcba9-8765-4321-0fed-cba987654321\"]")
    private Set<String> baseNameOfImgs;
}
