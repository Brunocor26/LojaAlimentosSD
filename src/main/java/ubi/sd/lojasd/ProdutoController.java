package ubi.sd.lojasd;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/produtos") // O endereço da nossa API
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Quando acederes a /api/produtos, ele vai a BD buscar tudo
    @GetMapping
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    // Rota POST para diminuir o stock de um produto
    @PostMapping("/{id}/diminuir-stock")
    public ResponseEntity<?> diminuirStock(@PathVariable Long id) {
        return repository.findById(id).map(produto -> {
            if (produto.getStock() > 0) {
                produto.setStock(produto.getStock() - 1);
                repository.save(produto);
                return ResponseEntity.ok(produto);
            } else {
                return ResponseEntity.badRequest().body("Erro: Produto sem stock disponível!");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Criar um novo Produto
    @PostMapping
    public ResponseEntity<Produto> criarProduto(@RequestBody Produto produto) {
        Produto novoProduto = repository.save(produto);
        return ResponseEntity.ok(novoProduto);
    }

    // Editar um Produto existente
    @PutMapping("/{id}")
    public ResponseEntity<Produto> editarProduto(@PathVariable Long id, @RequestBody Produto detalhes) {
        return repository.findById(id).map(produto -> {
            produto.setNome(detalhes.getNome());
            produto.setDescricao(detalhes.getDescricao());
            produto.setPreco(detalhes.getPreco());
            produto.setStock(detalhes.getStock());
            produto.setImagemUrl(detalhes.getImagemUrl());
            produto.setId_categoria(detalhes.getId_categoria());
            repository.save(produto);
            return ResponseEntity.ok(produto);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Remover um Produto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerProduto(@PathVariable Long id) {
        return repository.findById(id).map(produto -> {
            repository.delete(produto);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostConstruct
    public void popularBaseDeDados() {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria("Frutas"));
            categoriaRepository.save(new Categoria("Frutos Vermelhos"));
        }

        if (repository.count() == 0) {
            //nome, descricao, preco, stock, imagemUrl, id_categoria
            repository.save(new Produto("MAÇÃ", "Maçã Gala fresquinha", 0.50, 100, "img/apple.png", 1L));
            repository.save(new Produto("BANANA", "Banana da Madeira", 1.20, 50, "img/banana.png", 1L));
            repository.save(new Produto("MORANGO", "Morangos biológicos", 2.50, 20, "img/strawberry.png", 2L));

            System.out.println("Dados de teste inseridos no MariaDB com sucesso!");
        }
    }
}
