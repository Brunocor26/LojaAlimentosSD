package ubi.sd.lojasd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para o pedido de login.
 * Usado no endpoint POST /api/auth/login
 */
public record LoginRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "A password é obrigatória")
        String password
) {
}
