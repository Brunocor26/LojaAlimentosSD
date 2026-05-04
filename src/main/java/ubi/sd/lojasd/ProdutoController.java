package ubi.sd.lojasd;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos") // O endereço da nossa API
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    // Quando acederes a /api/produtos, ele vai ao Postgres buscar tudo
    @GetMapping
    public List<Produto> listarProdutos() {
        return repository.findAll();
    }

    // Este método corre automaticamente quando se liga a aplicacao
    // Insere dados de teste no PostgreSQL se a tabela estiver vazia
    @PostConstruct
    public void popularBaseDeDados() {
        if (repository.count() == 0) {
            repository.save(new Produto("MAÇÃ", 0.50, "https://images.unsplash.com/photo-1560806887-1e4cd0b6faa6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"));
            repository.save(new Produto("BANANA", 1.20, "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"));
            repository.save(new Produto("MORANGO", 2.50, "https://images.unsplash.com/photo-1528825871115-3581a5387919?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"));
            repository.save(new Produto("ANANÁS", 3.00, "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"));
            System.out.println("Produtos inseridos no PostgreSQL com sucesso!");
        }
    }
}