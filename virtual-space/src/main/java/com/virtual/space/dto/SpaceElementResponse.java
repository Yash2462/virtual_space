package com.virtual.space.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceElementResponse {
    private Long id;
    private ElementResponse element;
    private Integer x;
    private Integer y;
}
