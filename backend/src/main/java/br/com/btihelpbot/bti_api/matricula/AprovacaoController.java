package br.com.btihelpbot.bti_api.matricula;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints publicos de consulta da taxa de aprovacao (bot + site). */
@RestController
@RequestMapping("/api")
public class AprovacaoController {

    private static final int LIMITE = 50;
    private static final int DESTAQUES = 12;
    private static final int TAMANHO_PAGINA_MAXIMO = 100;

    private final AprovacaoService service;

    public AprovacaoController(AprovacaoService service) {
        this.service = service;
    }

    @GetMapping("/aprovacao/disciplina")
    public List<AprovacaoDTO> porDisciplina(@RequestParam String q,
                                            @RequestParam(defaultValue = "10") int minTotal) {
        return service.porDisciplina(q, minTotal, LIMITE);
    }

    @GetMapping("/aprovacao/docente")
    public List<AprovacaoDTO> porDocente(@RequestParam String q,
                                         @RequestParam(defaultValue = "10") int minTotal) {
        return service.porDocente(q, minTotal, LIMITE);
    }

    @GetMapping("/busca")
    public BuscaDTO buscar(@RequestParam String q,
                           @RequestParam(defaultValue = "0") int min,
                           @RequestParam(defaultValue = "taxa") String ordem) {
        return service.buscar(q, min, ordem);
    }

    @GetMapping("/turma/{codigo}")
    public ResponseEntity<TurmaDTO> turma(@PathVariable String codigo,
                                          @RequestParam(defaultValue = "taxa") String ordem) {
        return service.turma(codigo, ordem)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/professor/{slug}")
    public ResponseEntity<ProfessorDTO> professor(@PathVariable String slug,
                                                  @RequestParam(defaultValue = "taxa") String ordem) {
        return service.professor(slug, ordem)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/turmas")
    public TurmasPaginadasDTO turmas(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) String setor,
                                     @RequestParam(defaultValue = "0") int minTotal,
                                     @RequestParam(defaultValue = "nome") String ordem,
                                     @RequestParam(name = "one_page", defaultValue = "false") boolean onePage,
                                     @RequestParam(defaultValue = "0") int pagina,
                                     @RequestParam(defaultValue = "20") int tamanho) {
        int paginaValida = Math.max(pagina, 0);
        int tamanhoValido = Math.min(Math.max(tamanho, 1), TAMANHO_PAGINA_MAXIMO);
        return service.listarTurmas(q, setor, minTotal, ordem, onePage, paginaValida, tamanhoValido);
    }

    @GetMapping("/destaques")
    public List<DestaqueDTO> destaques() {
        return service.destaques(DESTAQUES);
    }

    @GetMapping("/aprovacao/cobertura")
    public ResponseEntity<CoberturaDTO> cobertura() {
        return service.cobertura()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
