package ubi.sd.lojasd.dto;
import ubi.sd.lojasd.model.Cliente;

/**
 * DTO de resposta com os dados do cliente autenticado.
 * NUNCA expõe a password — apenas os dados públicos.
 */
public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String role
) {
    // Construtor de conveniência a partir da entidade
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getRole().name()
        );
    }
}
