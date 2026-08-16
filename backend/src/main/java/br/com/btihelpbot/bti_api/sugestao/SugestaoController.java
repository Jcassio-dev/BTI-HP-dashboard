package br.com.btihelpbot.bti_api.sugestao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sugestao")
public class SugestaoController {

    private final SugestaoRepository repository;

    public SugestaoController(SugestaoRepository repository) {
        this.repository = repository;
    }

    public record CriarSugestao(String texto, String userId, String nome) {}

    public record SugestaoDTO(Long id, String texto, String userId, String nome, Instant criadoEm) {
        static SugestaoDTO from(Sugestao s) {
            return new SugestaoDTO(s.getId(), s.getTexto(), s.getUserId(), s.getNome(), s.getCriadoEm());
        }
    }

    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody CriarSugestao dto) {
        if (dto.texto() == null || dto.texto().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Sugestao s = new Sugestao();
        s.setTexto(dto.texto().trim());
        s.setUserId(dto.userId());
        s.setNome(dto.nome());
        repository.save(s);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<SugestaoDTO> listar() {
        return repository.findTop100ByOrderByCriadoEmDesc().stream()
                .map(SugestaoDTO::from)
                .toList();
    }
}
