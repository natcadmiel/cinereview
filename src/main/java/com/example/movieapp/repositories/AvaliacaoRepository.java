package com.example.movieapp.repositories;

import com.example.movieapp.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByIdFilme(String idFilme);

    List<Avaliacao> findByNomeUsuario(String nomeUsuario);
}
