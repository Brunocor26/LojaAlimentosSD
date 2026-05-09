package ubi.sd.lojasd;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desativar CSRF para permitir POST de ferramentas como Postman/Frontend simples
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permitir todas as rotas para facilitar o desenvolvimento
            )
            .httpBasic(withDefaults());
        
        return http.build();
    }
}
