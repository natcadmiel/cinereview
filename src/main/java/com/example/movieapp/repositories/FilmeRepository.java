package com.example.movieapp.repositories;

import com.example.movieapp.models.Filme;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FilmeRepository extends JpaRepository<Filme, String> {
    List<Filme> findByTituloContainingIgnoreCase(String titulo);

    List<Filme> findByUser(com.example.movieapp.models.User user);
}
