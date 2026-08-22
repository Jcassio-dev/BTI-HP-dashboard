package br.com.btihelpbot.bti_api.matricula;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class AprovacaoService {

    private static final Map<String, String> ROMANOS = Map.of(
            "i", "1", "ii", "2", "iii", "3", "iv", "4",
            "v", "5", "vi", "6", "vii", "7", "viii", "8");

    private static final Comparator<TaxaAprovacao> RANKING = Comparator
            .comparingDouble((TaxaAprovacao t) -> t.desfechos().taxaAprovacao())
            .thenComparingLong(t -> t.desfechos().totalMatriculados())
            .reversed();

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
        List<String> termos = tokens(q);
        if (termos.isEmpty()) {
            return List.of();
        }
        return repository.findAll().stream()
                .filter(t -> t.desfechos().totalMatriculados() >= minTotal)
                .filter(t -> t.getComponenteNome() != null && !t.getComponenteNome().isBlank())
                .filter(t -> t.getDocenteNome() != null && !t.getDocenteNome().isBlank())
                .filter(t -> matches(campo.apply(t), termos))
                .sorted(RANKING)
                .limit(limit)
                .map(AprovacaoDTO::from)
                .toList();
    }

    static boolean matches(String nome, List<String> termos) {
        List<String> nomeTokens = tokens(nome);
        for (String termo : termos) {
            boolean numerico = termo.chars().allMatch(Character::isDigit);
            boolean achou = numerico
                    ? nomeTokens.contains(termo)
                    : nomeTokens.stream().anyMatch(n -> n.contains(termo));
            if (!achou) return false;
        }
        return true;
    }

    static List<String> tokens(String s) {
        List<String> out = new ArrayList<>();
        for (String tok : normalizar(s).split("\\s+")) {
            if (tok.isEmpty()) continue;
            out.add(ROMANOS.getOrDefault(tok, tok));
        }
        return out;
    }

    static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
