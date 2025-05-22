package com.virtual.space.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateSpaceRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String dimensions;

    @NotBlank
    private String mapId;

    // getters, setters
}
