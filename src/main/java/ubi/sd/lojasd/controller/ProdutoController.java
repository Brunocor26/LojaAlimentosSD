package ubi.sd.lojasd.controller;

import ubi.sd.lojasd.dto.ProdutoRequest;
import ubi.sd.lojasd.model.Categoria;
import ubi.sd.lojasd.model.Produto;
import ubi.sd.lojasd.repository.CategoriaRepository;
import ubi.sd.lojasd.repository.ProdutoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public List<Produto> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<Produto> listarPorCategoria(@PathVariable Long categoriaId) {
        return repository.findByCategoriaId(categoriaId);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImagem(@RequestParam("imagem") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ficheiro vazio."));
        }

        try {
            // Criar diretório se não existir
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Gerar nome único para o ficheiro
            String extensao = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String nomeFicheiro = UUID.randomUUID().toString() + extensao;
            Path filePath = uploadPath.resolve(nomeFicheiro);

            // Guardar ficheiro
            Files.copy(file.getInputStream(), filePath);

            // Retornar o caminho relativo para ser usado no ProdutoRequest
            return ResponseEntity.ok(Map.of("caminho", "/uploads/" + nomeFicheiro));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro ao guardar imagem: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody ProdutoRequest pedido) {
        Categoria categoria = categoriaRepository.findById(pedido.categoriaId())
                .orElse(null);
        if (categoria == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Categoria não encontrada."));
        }
        Produto produto = new Produto(
                pedido.nome(), pedido.descricao(), pedido.preco(),
                pedido.stock(), pedido.imagemUrl(), categoria
        );
        return ResponseEntity.status(201).body(repository.save(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest pedido) {
        return repository.findById(id).map(produto -> {
            Categoria categoria = categoriaRepository.findById(pedido.categoriaId())
                    .orElse(null);
            if (categoria == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Categoria não encontrada."));
            }
            produto.setNome(pedido.nome());
            produto.setDescricao(pedido.descricao());
            produto.setPreco(pedido.preco());
            produto.setStock(pedido.stock());
            produto.setImagemUrl(pedido.imagemUrl());
            produto.setCategoria(categoria);
            return ResponseEntity.ok(repository.save(produto));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
