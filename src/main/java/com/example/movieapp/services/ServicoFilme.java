package com.example.movieapp.services;

import com.example.movieapp.models.Filme;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicoFilme {

    private final String SEARCH_URL = "https://search.imdbot.workers.dev/?q=";
    private final String DETAIL_URL = "https://search.imdbot.workers.dev/?tt=";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Filme> buscarFilmes(String query) {
        List<Filme> filmes = new ArrayList<>();
        try {
            String jsonResponse = restTemplate.getForObject(SEARCH_URL + query, String.class);
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode description = root.path("description");

            if (description.isArray()) {
                for (JsonNode node : description) {
                    String titulo = node.path("#TITLE").asText();
                    String ano = node.path("#YEAR").asText();
                    String imagem = node.path("#IMG_POSTER").asText();
                    String idImdb = node.path("#IMDB_ID").asText();
                    String atores = node.path("#ACTORS").asText();

                    Filme filme = new Filme(titulo, ano, atores, imagem);
                    filme.setId(idImdb);
                    filmes.add(filme);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filmes;
    }

    public Filme obterDetalhesFilme(String idImdb) {
        try {
            String jsonResponse = restTemplate.getForObject(DETAIL_URL + idImdb, String.class);
            JsonNode root = mapper.readTree(jsonResponse);

            String titulo = root.path("short").path("name").asText();
            String ano = root.path("top").path("releaseDate").path("year").asText();
            if (ano.isEmpty())
                ano = root.path("short").path("datePublished").asText();

            String imagem = root.path("short").path("image").asText();
            String descricao = root.path("short").path("description").asText();

            if (descricao.isEmpty())
                descricao = root.path("main").path("plot").path("text").asText();

            Filme filme = new Filme(titulo, ano, descricao, imagem);
            filme.setId(idImdb);
            return filme;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
