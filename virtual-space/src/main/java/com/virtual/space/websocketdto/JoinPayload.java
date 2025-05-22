package com.virtual.space.websocketdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinPayload {
    private String spaceId;
    private String token;
}
