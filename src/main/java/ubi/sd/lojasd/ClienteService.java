package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço de negócio para gestão de clientes e autenticação.
 *
 * Implementa UserDetailsService para integração com o Spring Security —
 * permite que o Spring carregue o utilizador a partir do email durante o login.
 */
@Service
public class ClienteService implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Regista um novo cliente na base de dados.
     * A password é guardada com hash BCrypt.
     *
     * @throws IllegalArgumentException se o email já estiver registado
     */
    public ClienteResponse registar(RegistarRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe uma conta com este email.");
        }

        Cliente cliente = new Cliente(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Cliente.Role.CLIENTE
        );

        Cliente guardado = clienteRepository.save(cliente);
        return ClienteResponse.from(guardado);
    }

    /**
     * Devolve os dados do cliente pelo email.
     * Usado no endpoint /api/auth/me para mostrar o utilizador autenticado.
     */
    public ClienteResponse buscarPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado: " + email));
        return ClienteResponse.from(cliente);
    }

    /**
     * Implementação exigida pelo Spring Security.
     * Carrega os detalhes do utilizador (email + password + roles) para autenticação.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));

        return new User(
                cliente.getEmail(),
                cliente.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + cliente.getRole().name()))
        );
    }
}
