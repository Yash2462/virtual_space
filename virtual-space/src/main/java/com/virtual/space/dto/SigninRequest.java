package com.virtual.space.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SigninRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // getters, setters
}
