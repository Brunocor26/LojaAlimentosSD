package ubi.sd.lojasd.controller;
import ubi.sd.lojasd.repository.ProdutoRepository;
import ubi.sd.lojasd.model.Produto;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

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

    @PostConstruct
    public void popularBaseDeDados() {
        if (categoriaRepository.count() == 0) {
            Categoria frutas = categoriaRepository.save(new Categoria("FRUTAS", "Frutas frescas e biológicas"));
            Categoria vegetais = categoriaRepository.save(new Categoria("VEGETAIS", "Vegetais frescos da época"));

            repository.save(new Produto("MAÇÃ", "Maçã Gala fresquinha", 0.50, 100, "img/apple.png", frutas));
            repository.save(new Produto("BANANA", "Banana da Madeira", 1.20, 50, "img/banana.png", frutas));
            repository.save(new Produto("MORANGO", "Morangos biológicos", 2.50, 20, "img/strawberry.png", vegetais));

            System.out.println("Categorias e produtos de teste inseridos com sucesso!");
        }
    }
}
