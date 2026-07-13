package br.com.btihelpbot.bti_api.matricula;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Consulta da taxa de aprovacao. A tabela e pequena (poucos milhares de linhas),
 * entao busca em memoria com normalizacao (sem acento, case-insensitive).
 */
@Service
public class AprovacaoService {

    private final TaxaAprovacaoRepository repository;

    public AprovacaoService(TaxaAprovacaoRepository repository) {
        this.repository = repository;
    }

    public List<AprovacaoDTO> porDisciplina(String q, int minTotal, int limit) {
        return buscar(TaxaAprovacao::getComponenteNome, q, minTotal, limit);
    }

    public List<AprovacaoDTO> porDocente(String q, int minTotal, int limit) {
        return buscar(TaxaAprovacao::getDocenteNome, q, minTotal, limit);
    }

    private List<AprovacaoDTO> buscar(Function<TaxaAprovacao, String> campo, String q, int minTotal, int limit) {
        String termo = normalizar(q);
        if (termo.isEmpty()) {
            return List.of();
        }
        return repository.findAll().stream()
                .filter(t -> t.getTotal() >= minTotal)
                .filter(t -> normalizar(campo.apply(t)).contains(termo))
                .sorted(Comparator.comparingDouble(TaxaAprovacao::getTaxa).reversed()
                        .thenComparing(Comparator.comparingLong(TaxaAprovacao::getTotal).reversed()))
                .limit(limit)
                .map(AprovacaoDTO::from)
                .toList();
    }

    /** minusculas, sem acento, trim. */
    static String normalizar(String s) {
        if (s == null) return "";
        String semAcento = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().trim();
    }
}
