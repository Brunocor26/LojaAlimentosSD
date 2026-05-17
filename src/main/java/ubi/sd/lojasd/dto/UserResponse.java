package ubi.sd.lojasd.dto;

import ubi.sd.lojasd.model.User;

/**
 * DTO de resposta com os dados do utilizador autenticado.
 */
public record UserResponse(
        Long id,
        String nome,
        String email,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
