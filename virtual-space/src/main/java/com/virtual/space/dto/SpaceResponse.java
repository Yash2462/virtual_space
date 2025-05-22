package com.virtual.space.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceResponse {
    private Long id;
    private String name;
    private String dimensions;
    private String thumbnail;

    // constructors, getters, setters
}
