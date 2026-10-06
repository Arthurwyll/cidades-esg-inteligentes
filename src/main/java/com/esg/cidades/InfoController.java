package com.esg.cidades;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Informa em qual ambiente (local, staging, production) a aplicacao esta rodando. */
@RestController
public class InfoController {

    private final String ambiente;

    public InfoController(@Value("${app.environment}") String ambiente) {
        this.ambiente = ambiente;
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of("aplicacao", "Cidades ESG Inteligentes", "ambiente", ambiente);
    }
}
