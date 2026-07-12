package br.com.btihelpbot.bti_api.matricula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/matricula")
public class IngestaoController {

    private static final Logger log = LoggerFactory.getLogger(IngestaoController.class);

    private final IngestaoService service;

    public IngestaoController(IngestaoService service) {
        this.service = service;
    }

    /** Dispara a ingestao em background (leva minutos). Protegido por X-API-Key. */
    @PostMapping("/ingestao")
    public ResponseEntity<Map<String, Object>> ingerir() {
        CompletableFuture.runAsync(() -> {
            try {
                service.ingerir();
            } catch (Exception e) {
                log.error("Ingestao de matricula falhou", e);
            }
        });
        return ResponseEntity.accepted().body(Map.of("status", "iniciada"));
    }
}
