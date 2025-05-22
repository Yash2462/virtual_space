package com.virtual.space.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
//    private long expiration;
}
