package br.com.btihelpbot.bti_api.matricula;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class AprovacaoService {

    private static final Map<String, String> ROMANOS = Map.of(
            "i", "1", "ii", "2", "iii", "3", "iv", "4",
            "v", "5", "vi", "6", "vii", "7", "viii", "8");

    private static final Comparator<TaxaAprovacao> POR_TAXA = Comparator
            .comparingDouble((TaxaAprovacao t) -> t.desfechos().taxaAprovacao())
            .thenComparingLong(t -> t.desfechos().totalMatriculados())
            .reversed();

    private static final Comparator<TaxaAprovacao> POR_ALUNOS = Comparator
            .comparingLong((TaxaAprovacao t) -> t.desfechos().totalMatriculados())
            .thenComparingDouble(t -> t.desfechos().taxaAprovacao())
            .reversed();

    private static final Comparator<TaxaAprovacao> POR_NOME = Comparator
            .comparing((TaxaAprovacao t) -> normalizar(t.getComponenteNome()))
            .thenComparing(t -> normalizar(t.getDocenteNome()));

    private final TaxaAprovacaoRepository repository;
    private final ComponenteRepository componentes;

    public AprovacaoService(TaxaAprovacaoRepository repository, ComponenteRepository componentes) {
        this.repository = repository;
        this.componentes = componentes;
    }

    public List<AprovacaoDTO> porDisciplina(String q, int minTotal, int limit) {
        return buscarPorCampo(TaxaAprovacao::getComponenteNome, q, minTotal, limit);
    }

    public List<AprovacaoDTO> porDocente(String q, int minTotal, int limit) {
        return buscarPorCampo(TaxaAprovacao::getDocenteNome, q, minTotal, limit);
    }

    /** Campo unico: casa codigo, nome da disciplina e nome do professor de uma vez. */
    public BuscaDTO buscar(String q, int minTotal, String ordem) {
        List<String> termos = tokens(q);
        if (termos.isEmpty()) {
            return new BuscaDTO(List.of(), List.of());
        }

        List<TaxaAprovacao> disciplinas = new ArrayList<>();
        List<TaxaAprovacao> professores = new ArrayList<>();

        for (TaxaAprovacao t : candidatos(minTotal)) {
            if (casaDisciplina(t, termos)) {
                disciplinas.add(t);
            } else if (matches(t.getDocenteNome(), termos)) {
                professores.add(t);
            }
        }

        Comparator<TaxaAprovacao> cmp = comparador(ordem);
        return new BuscaDTO(ordenar(disciplinas, cmp), ordenar(professores, cmp));
    }

    public Optional<TurmaDTO> turma(String codigo, String ordem) {
        String alvo = normalizar(codigo);
        Optional<Componente> componente = componentes.findAll().stream()
                .filter(c -> normalizar(c.getCodigo()).equals(alvo))
                .findFirst();

        return componente.map(c -> {
            List<TaxaAprovacao> turmas = repository.findAll().stream()
                    .filter(t -> normalizar(t.getComponenteCodigo()).equals(alvo))
                    .toList();
            return TurmaDTO.de(c, ordenar(turmas, comparador(ordem)));
        });
    }

    public Optional<ProfessorDTO> professor(String slug, String ordem) {
        List<TaxaAprovacao> turmas = repository.findAll().stream()
                .filter(t -> slug.equals(t.getDocenteSlug()))
                .toList();
        if (turmas.isEmpty()) {
            return Optional.empty();
        }
        Desfechos total = turmas.stream()
                .map(TaxaAprovacao::desfechos)
                .reduce(Desfechos.ZERO, Desfechos::mais);
        String nome = turmas.get(0).getDocenteNome();
        return Optional.of(ProfessorDTO.de(slug, nome, total, ordenar(turmas, comparador(ordem))));
    }

    public List<DestaqueDTO> destaques(int limite) {
        return componentes.findAll().stream()
                .filter(c -> c.getNome() != null && !c.getNome().isBlank())
                .sorted(Comparator.comparingLong((Componente c) -> c.desfechos().totalMatriculados()).reversed())
                .limit(limite)
                .map(DestaqueDTO::de)
                .toList();
    }

    private List<TaxaAprovacao> candidatos(int minTotal) {
        return repository.findAll().stream()
                .filter(t -> t.desfechos().totalMatriculados() >= minTotal)
                .filter(t -> t.getComponenteNome() != null && !t.getComponenteNome().isBlank())
                .filter(t -> t.getDocenteNome() != null && !t.getDocenteNome().isBlank())
                .toList();
    }

    private List<AprovacaoDTO> buscarPorCampo(Function<TaxaAprovacao, String> campo,
                                              String q, int minTotal, int limit) {
        List<String> termos = tokens(q);
        if (termos.isEmpty()) {
            return List.of();
        }
        return candidatos(minTotal).stream()
                .filter(t -> matches(campo.apply(t), termos))
                .sorted(POR_TAXA)
                .limit(limit)
                .map(AprovacaoDTO::from)
                .toList();
    }

    private static List<AprovacaoDTO> ordenar(List<TaxaAprovacao> linhas, Comparator<TaxaAprovacao> cmp) {
        return linhas.stream().sorted(cmp).map(AprovacaoDTO::from).toList();
    }

    private static Comparator<TaxaAprovacao> comparador(String ordem) {
        if ("alunos".equals(ordem)) return POR_ALUNOS;
        if ("nome".equals(ordem)) return POR_NOME;
        return POR_TAXA;
    }

    private static boolean casaDisciplina(TaxaAprovacao t, List<String> termos) {
        return matches(t.getComponenteNome(), termos) || matches(t.getComponenteCodigo(), termos);
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
