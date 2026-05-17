package ubi.sd.lojasd.controller;
import ubi.sd.lojasd.service.UserService;
import ubi.sd.lojasd.dto.LoginRequest;
import ubi.sd.lojasd.dto.RegistarRequest;
import ubi.sd.lojasd.dto.UserResponse;

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
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/registar")
    public ResponseEntity<?> registar(@Valid @RequestBody RegistarRequest request) {
        try {
            UserResponse resposta = userService.registar(request);
            return ResponseEntity.status(201).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);

            new HttpSessionSecurityContextRepository()
                    .saveContext(contexto, httpRequest, httpResponse);

            UserResponse resposta = userService.buscarPorEmail(request.email());
            return ResponseEntity.ok(resposta);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("erro", "Email ou password incorretos."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        var sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("mensagem", "Sessão terminada com sucesso."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado."));
        }
        UserResponse resposta = userService.buscarPorEmail(principal.getName());
        return ResponseEntity.ok(resposta);
    }
}
