package com.virtual.space.websocketdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceJoinedResponse {
    private String type = "space-joined";
    private SpaceJoinedPayload payload = new SpaceJoinedPayload();
}
