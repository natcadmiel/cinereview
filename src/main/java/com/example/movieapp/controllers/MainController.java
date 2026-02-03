package com.example.movieapp.controllers;

import com.example.movieapp.models.Avaliacao;
import com.example.movieapp.models.Filme;
import com.example.movieapp.models.User;
import com.example.movieapp.repositories.AvaliacaoRepository;
import com.example.movieapp.repositories.FilmeRepository;
import com.example.movieapp.services.ServicoFilme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private ServicoFilme servicoFilme;

    @Autowired
    private FilmeRepository filmeRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private com.example.movieapp.repositories.UserRepository userRepository;

    @GetMapping("/")
    public String inicio(@RequestParam(value = "query", required = false) String query, Model model,
            java.security.Principal principal) {
        List<Filme> filmes = new ArrayList<>();
        if (query != null && !query.isEmpty()) {
            filmes.addAll(filmeRepository.findByTituloContainingIgnoreCase(query));
            filmes.addAll(servicoFilme.buscarFilmes(query));
            model.addAttribute("query", query);
        } else {

        }

        if (query == null || query.isEmpty()) {
            filmes.addAll(filmeRepository.findAll());
        }

        model.addAttribute("filmes", filmes);
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("currentUser", user);
        }
        return "index";
    }

    @GetMapping("/meus-filmes")
    public String meusFilmes(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            List<Filme> filmes = filmeRepository.findByUser(user);
            model.addAttribute("filmes", filmes);
            model.addAttribute("query", "Meus Filmes");
            model.addAttribute("currentUser", user);
        }
        return "index";
    }

    @GetMapping("/minhas-avaliacoes")
    public String minhasAvaliacoes(Model model, java.security.Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByNomeUsuario(username);

        List<Map<String, Object>> avaliacoesComFilme = new ArrayList<>();

        for (Avaliacao avaliacao : avaliacoes) {
            Filme filme = servicoFilme.obterDetalhesFilme(avaliacao.getIdFilme());
            Map<String, Object> item = new HashMap<>();
            item.put("avaliacao", avaliacao);
            item.put("filme", filme);
            avaliacoesComFilme.add(item);
        }

        model.addAttribute("avaliacoesComFilme", avaliacoesComFilme);
        model.addAttribute("currentUser", userRepository.findByUsername(username).orElse(null));
        return "minhas-avaliacoes";
    }

    @PostMapping("/")
    public String buscar(@RequestParam("query") String query, RedirectAttributes redirectAttributes) {
        return "redirect:/?query=" + query;
    }

    @GetMapping("/filme/{id}")
    public String detalhesFilme(@PathVariable("id") String id, Model model,
            RedirectAttributes redirectAttributes,
            java.security.Principal principal) {
        Filme filme = null;

        if (id.startsWith("loc_")) {
            Optional<Filme> local = filmeRepository.findById(id);
            if (local.isPresent())
                filme = local.get();
        } else {
            filme = servicoFilme.obterDetalhesFilme(id);
        }

        if (filme == null) {
            redirectAttributes.addFlashAttribute("error", "Detalhes do filme não encontrados.");
            return "redirect:/";
        }

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByIdFilme(id);
        model.addAttribute("filme", filme);
        model.addAttribute("avaliacoes", avaliacoes);

        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("currentUser", user);
        }

        return "movie";
    }

    @GetMapping("/cadastrar")
    public String formCadastro(Model model) {
        model.addAttribute("filme", new Filme());
        return "cadastrar";
    }

    @PostMapping("/cadastrar")
    public String cadastrarFilme(Filme filme, RedirectAttributes redirectAttributes,
            java.security.Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            filme.setUser(user);
        }
        filmeRepository.save(filme);
        return "redirect:/filme/" + filme.getId();
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable("id") String id, Model model, java.security.Principal principal,
            RedirectAttributes redirectAttributes) {
        Optional<Filme> filmeOpt = filmeRepository.findById(id);
        if (filmeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Filme não encontrado.");
            return "redirect:/";
        }
        Filme filme = filmeOpt.get();

        if (principal == null || filme.getUser() == null
                || !filme.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para editar este filme.");
            return "redirect:/";
        }

        model.addAttribute("filme", filme);
        return "cadastrar";
    }

    @PostMapping("/excluir/{id}")
    public String excluirFilme(@PathVariable("id") String id, java.security.Principal principal,
            RedirectAttributes redirectAttributes) {
        Optional<Filme> filmeOpt = filmeRepository.findById(id);
        if (filmeOpt.isPresent()) {
            Filme filme = filmeOpt.get();
            if (principal != null && filme.getUser() != null
                    && filme.getUser().getUsername().equals(principal.getName())) {
                filmeRepository.delete(filme);
                redirectAttributes.addFlashAttribute("success", "Filme excluído com sucesso.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Permissão negada.");
            }
        }
        return "redirect:/";
    }

    @PostMapping("/filme/{id}/adicionar_avaliacao")
    public String adicionarAvaliacao(@PathVariable("id") String id,
            @RequestParam("user_name") String nomeUsuario,
            @RequestParam("rating") Integer nota,
            @RequestParam("comment") String comentario,
            RedirectAttributes redirectAttributes,
            java.security.Principal principal) {

        String usuarioAutor = nomeUsuario;
        if (principal != null) {
            usuarioAutor = principal.getName();
        }

        Avaliacao avaliacao = new Avaliacao(id, usuarioAutor, nota, comentario);
        avaliacaoRepository.save(avaliacao);
        redirectAttributes.addFlashAttribute("success", "Avaliação adicionada com sucesso!");
        return "redirect:/filme/" + id;
    }

    @GetMapping("/avaliacao/editar/{id}")
    public String editarAvaliacao(@PathVariable("id") Long id, Model model,
            java.security.Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findById(id);
        if (avaliacaoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Avaliação não encontrada.");
            return "redirect:/minhas-avaliacoes";
        }
        Avaliacao avaliacao = avaliacaoOpt.get();

        if (principal == null || !avaliacao.getNomeUsuario().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Você não tem permissão para editar esta avaliação.");
            return "redirect:/minhas-avaliacoes";
        }

        Filme filme = servicoFilme.obterDetalhesFilme(avaliacao.getIdFilme());
        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("filme", filme);
        return "editar-avaliacao";
    }

    @PostMapping("/avaliacao/editar/{id}")
    public String atualizarAvaliacao(@PathVariable("id") Long id,
            @RequestParam("rating") Integer nota,
            @RequestParam("comment") String comentario,
            java.security.Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findById(id);
        if (avaliacaoOpt.isPresent()) {
            Avaliacao avaliacao = avaliacaoOpt.get();
            if (principal != null && avaliacao.getNomeUsuario().equals(principal.getName())) {
                avaliacao.setNota(nota);
                avaliacao.setComentario(comentario);
                avaliacaoRepository.save(avaliacao);
                redirectAttributes.addFlashAttribute("success", "Avaliação atualizada com sucesso.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Permissão negada.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Avaliação não encontrada.");
        }
        return "redirect:/minhas-avaliacoes";
    }

    @PostMapping("/avaliacao/excluir/{id}")
    public String excluirAvaliacao(@PathVariable("id") Long id,
            java.security.Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Avaliacao> avaliacaoOpt = avaliacaoRepository.findById(id);
        if (avaliacaoOpt.isPresent()) {
            Avaliacao avaliacao = avaliacaoOpt.get();
            if (principal != null && avaliacao.getNomeUsuario().equals(principal.getName())) {
                avaliacaoRepository.delete(avaliacao);
                redirectAttributes.addFlashAttribute("success", "Avaliação excluída com sucesso.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Permissão negada.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Avaliação não encontrada.");
        }
        return "redirect:/minhas-avaliacoes";
    }
}
