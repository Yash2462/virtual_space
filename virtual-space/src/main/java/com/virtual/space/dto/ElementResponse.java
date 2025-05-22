package com.virtual.space.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElementResponse {
    private String id;
    private String imageUrl;
    private Boolean isStatic;
    private Integer height;
    private Integer width;

    // constructors, getters, setters
}
