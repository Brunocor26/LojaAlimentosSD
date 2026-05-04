package ubi.sd.lojasd;

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
    private double preco;
    private String imagemUrl; //foto

    public Produto() {
    }

    public Produto(String nome, double preco, String imagemUrl) {
        this.nome = nome;
        this.preco = preco;
        this.imagemUrl = imagemUrl;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public String getImagemUrl() { return imagemUrl; }
}