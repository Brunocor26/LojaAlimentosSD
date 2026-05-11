package ubi.sd.lojasd;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Controller REST para autenticação de utilizadores.
 *
 * Endpoints disponíveis:
 *  POST /api/auth/registar  — criar conta (público)
 *  POST /api/auth/login     — iniciar sessão (público)
 *  POST /api/auth/logout    — terminar sessão (autenticado)
 *  GET  /api/auth/me        — dados do utilizador atual (autenticado)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Regista um novo cliente.
     * Retorna 201 Created com os dados do cliente criado (sem password).
     */
    @PostMapping("/registar")
    public ResponseEntity<?> registar(@Valid @RequestBody RegistarRequest request) {
        try {
            ClienteResponse resposta = clienteService.registar(request);
            return ResponseEntity.status(201).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    /**
     * Faz login com email e password.
     * Cria uma sessão HTTP e guarda o contexto de segurança.
     * Retorna os dados do cliente autenticado.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            // Autenticar as credenciais
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Guardar na sessão HTTP
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);

            // Persistir a sessão no pedido HTTP
            new HttpSessionSecurityContextRepository()
                    .saveContext(contexto, httpRequest, httpResponse);

            // Devolver dados do utilizador (sem password)
            ClienteResponse resposta = clienteService.buscarPorEmail(request.email());
            return ResponseEntity.ok(resposta);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("erro", "Email ou password incorretos."));
        }
    }

    /**
     * Termina a sessão do utilizador autenticado.
     * Invalida a sessão HTTP e limpa o contexto de segurança.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Invalidar a sessão HTTP
        var sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("mensagem", "Sessão terminada com sucesso."));
    }

    /**
     * Devolve os dados do utilizador atualmente autenticado.
     * Retorna 401 se não houver sessão ativa.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado."));
        }
        ClienteResponse resposta = clienteService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(resposta);
    }
}
