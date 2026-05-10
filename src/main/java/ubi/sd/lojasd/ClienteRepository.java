package ubi.sd.lojasd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade Cliente.
 * O Spring Data gera automaticamente as queries SQL.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar cliente por email (usado no login e no Spring Security)
    Optional<Cliente> findByEmail(String email);

    // Verificar se já existe um email registado
    boolean existsByEmail(String email);
}
