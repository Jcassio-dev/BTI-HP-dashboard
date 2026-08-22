package br.com.btihelpbot.bti_api.matricula;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AprovacaoAggregator {

    public record Key(long componenteId, String siape) {}

    private final Set<String> cursos;
    private final Map<Key, long[]> counts = new HashMap<>();
    private final Set<String> seen = new HashSet<>();

    public AprovacaoAggregator(Set<String> cursos) {
        this.cursos = cursos;
    }

    public void accumulate(Map<Long, TurmaInfo> turmasDoSemestre, MatriculaRow row) {
        if (!cursos.contains(row.idCurso())) {
            return;
        }
        String dedupKey = row.idTurma() + ":" + row.discente();
        if (!seen.add(dedupKey)) {
            return;
        }
        TurmaInfo turma = turmasDoSemestre.get(row.idTurma());
        if (turma == null) {
            return;
        }
        SituacaoClassifier.Categoria cat = SituacaoClassifier.classificar(row.situacao());
        if (cat == SituacaoClassifier.Categoria.IGNORADO) {
            return;
        }

        long[] c = counts.computeIfAbsent(new Key(turma.idComponente(), turma.siape()), k -> new long[4]);
        switch (cat) {
            case APROVADO -> c[0]++;
            case REPROVADO_NOTA -> c[1]++;
            case REPROVADO_FALTA -> c[2]++;
            case TRANCADO -> c[3]++;
            case IGNORADO -> { }
        }
    }

    /** Desfechos de cada par (disciplina, professor). */
    public Map<Key, Desfechos> getDesfechos() {
        Map<Key, Desfechos> out = new HashMap<>();
        counts.forEach((k, c) -> out.put(k, desfechos(c)));
        return out;
    }

    /** Desfechos da disciplina inteira, somando todos os professores. */
    public Map<Long, Desfechos> getDesfechosPorComponente() {
        Map<Long, Desfechos> out = new HashMap<>();
        counts.forEach((k, c) -> out.merge(k.componenteId(), desfechos(c), Desfechos::mais));
        return out;
    }

    private static Desfechos desfechos(long[] c) {
        return new Desfechos(c[0], c[1], c[2], c[3]);
    }
}
