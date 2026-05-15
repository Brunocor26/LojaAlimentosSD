package ubi.sd.lojasd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para o pedido de registo de um novo cliente.
 * Usado no endpoint POST /api/auth/registar
 */
public record RegistarRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "A password é obrigatória")
        @Size(min = 6, message = "A password deve ter no mínimo 6 caracteres")
        String password
) {
}
