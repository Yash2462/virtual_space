package com.virtual.space.websocketdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovePayload {
    private Integer x;
    private Integer y;
}
