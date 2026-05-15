package ubi.sd.lojasd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação com Spring Security.
 *
 * Regras de acesso:
 *  - Público:      GET /api/produtos, GET /api/categorias, POST /api/auth/registar, POST /api/auth/login
 *  - Autenticado:  POST /api/auth/logout, GET /api/auth/me, /api/carrinho/**, POST /api/vendas
 *  - ADMIN:        POST/PUT/DELETE /api/produtos, GET /api/estatisticas/**
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ClienteService clienteService;

    /**
     * Encoder BCrypt para as passwords.
     * Custo 10 é o valor padrão e adequado para produção.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provider de autenticação que usa o ClienteService para carregar
     * o utilizador e o BCrypt para verificar a password.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(clienteService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager exposto como Bean para ser injetado no AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cadeia de filtros de segurança HTTP.
     * Define as regras de autorização e a gestão de sessões.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desativar CSRF para API REST com frontend simples
            .csrf(csrf -> csrf.disable())

            // Regras de autorização por endpoint
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos (HTML, CSS, JS)
                .requestMatchers("/", "/index.html", "/login.html", "/registar.html", "/css/**", "/js/**", "/images/**").permitAll()

                // Autenticação — público
                .requestMatchers(HttpMethod.POST, "/api/auth/registar", "/api/auth/login").permitAll()

                // Produtos e categorias — leitura pública
                .requestMatchers(HttpMethod.GET, "/api/produtos/**", "/api/categorias/**").permitAll()

                // Gestão de produtos e categorias — apenas ADMIN
                .requestMatchers(HttpMethod.POST, "/api/produtos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/produtos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")

                // Estatísticas — apenas ADMIN
                .requestMatchers("/api/estatisticas/**").hasRole("ADMIN")

                // Tudo o resto requer autenticação
                .anyRequest().authenticated()
            )

            // Usar o nosso provider de autenticação
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
