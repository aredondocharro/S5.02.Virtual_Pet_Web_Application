package cat.itacademy.s05.t02.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthCreateUserRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String password
) { }