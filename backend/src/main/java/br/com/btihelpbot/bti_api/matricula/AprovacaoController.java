package br.com.btihelpbot.bti_api.matricula;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints publicos de consulta da taxa de aprovacao (bot + dashboard). */
@RestController
@RequestMapping("/api/aprovacao")
public class AprovacaoController {

    private static final int LIMITE = 50;

    private final AprovacaoService service;

    public AprovacaoController(AprovacaoService service) {
        this.service = service;
    }

    @GetMapping("/disciplina")
    public List<AprovacaoDTO> porDisciplina(@RequestParam String q,
                                            @RequestParam(defaultValue = "10") int minTotal) {
        return service.porDisciplina(q, minTotal, LIMITE);
    }

    @GetMapping("/docente")
    public List<AprovacaoDTO> porDocente(@RequestParam String q,
                                         @RequestParam(defaultValue = "10") int minTotal) {
        return service.porDocente(q, minTotal, LIMITE);
    }
}
