package com.example.movieapp.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Filme {

    @Id
    private String id;
    private String titulo;
    private String ano;
    private String descricao;

    @jakarta.persistence.Column(columnDefinition = "CLOB")
    private String imagem;

    public Filme() {
        this.id = "loc_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Filme(String titulo, String ano, String descricao, String imagem) {
        this();
        this.titulo = titulo;
        this.ano = ano;
        this.descricao = descricao;
        this.imagem = imagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
