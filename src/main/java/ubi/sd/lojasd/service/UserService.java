package ubi.sd.lojasd.service;
import ubi.sd.lojasd.model.User;
import ubi.sd.lojasd.dto.RegistarRequest;
import ubi.sd.lojasd.dto.UserResponse;
import ubi.sd.lojasd.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço de negócio para gestão de utilizadores e autenticação.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Regista um novo utilizador na base de dados.
     */
    public UserResponse registar(RegistarRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe uma conta com este email.");
        }

        User.Role roleToAssign = User.Role.CLIENTE;
        if (request.role() != null && request.role().equalsIgnoreCase("ADMIN")) {
            roleToAssign = User.Role.ADMIN;
        }

        User user = new User(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.password()),
                roleToAssign
        );

        User guardado = userRepository.save(user);
        return UserResponse.from(guardado);
    }

    /**
     * Devolve os dados do utilizador pelo email.
     */
    public UserResponse buscarPorEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));
        return UserResponse.from(user);
    }

    /**
     * Implementação exigida pelo Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
