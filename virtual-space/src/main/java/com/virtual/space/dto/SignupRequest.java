package com.virtual.space.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignupRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private String type; // "admin" or "user"

    // getters, setters
}
