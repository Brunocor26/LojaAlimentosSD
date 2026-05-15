package ubi.sd.lojasd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String descricao;
    private double preco;
    private int stock;
    private String imagemUrl;
    private Long id_categoria;

    // Construtor vazio obrigatório pela JPA
    public Produto() {
    }

    public Produto(String nome, String descricao, double preco, int stock, String imagemUrl, Long id_categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.stock = stock;
        this.imagemUrl = imagemUrl;
        this.id_categoria = id_categoria;
    }

    // Getters e Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getNome() { 
        return nome; 
    }

    public void setNome(String nome) { 
        this.nome = nome; 
    }

    public String getDescricao() { 
        return descricao; 
    }

    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }

    public double getPreco() { 
        return preco; 
    }

    public void setPreco(double preco) { 
        this.preco = preco; 
    }

    public int getStock() { 
        return stock; 
    }

    public void setStock(int stock) { 
        this.stock = stock; 
    }

    public String getImagemUrl() { 
        return imagemUrl; 
    }

    public void setImagemUrl(String imagemUrl) { 
        this.imagemUrl = imagemUrl; 
    }

    public Long getId_categoria() { 
        return id_categoria; 
    }

    public void setId_categoria(Long id_categoria) { 
        this.id_categoria = id_categoria; 
    }
}