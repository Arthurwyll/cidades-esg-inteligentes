package com.esg.cidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    private String titulo;

    @Size(max = 1000)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @NotBlank
    @Size(max = 80)
    private String bairro;

    @Enumerated(EnumType.STRING)
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;

    private LocalDateTime dataRegistro;

    @PrePersist
    void aoCriar() {
        this.dataRegistro = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusOcorrencia.ABERTA;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }
    public LocalDateTime getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDateTime dataRegistro) { this.dataRegistro = dataRegistro; }
}
