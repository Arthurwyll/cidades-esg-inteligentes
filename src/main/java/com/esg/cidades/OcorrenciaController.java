package com.esg.cidades;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaRepository repository;

    public OcorrenciaController(OcorrenciaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Ocorrencia> listar(@RequestParam(required = false) Categoria categoria) {
        return categoria == null ? repository.findAll() : repository.findByCategoria(categoria);
    }

    @GetMapping("/{id}")
    public Ocorrencia buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(this::naoEncontrada);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ocorrencia criar(@Valid @RequestBody Ocorrencia ocorrencia) {
        ocorrencia.setId(null);
        return repository.save(ocorrencia);
    }

    @PutMapping("/{id}")
    public Ocorrencia atualizar(@PathVariable Long id, @Valid @RequestBody Ocorrencia dados) {
        Ocorrencia existente = repository.findById(id).orElseThrow(this::naoEncontrada);
        existente.setTitulo(dados.getTitulo());
        existente.setDescricao(dados.getDescricao());
        existente.setCategoria(dados.getCategoria());
        existente.setBairro(dados.getBairro());
        if (dados.getStatus() != null) {
            existente.setStatus(dados.getStatus());
        }
        return repository.save(existente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw naoEncontrada();
        }
        repository.deleteById(id);
    }

    private ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocorrencia nao encontrada");
    }
}
