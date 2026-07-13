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
        SituacaoClassifier.Bucket bucket = SituacaoClassifier.classify(row.situacao());
        if (bucket == SituacaoClassifier.Bucket.IGNORADO) {
            return;
        }
        long[] c = counts.computeIfAbsent(new Key(turma.idComponente(), turma.siape()), k -> new long[2]);
        if (bucket == SituacaoClassifier.Bucket.APROVADO) {
            c[0]++;
        } else {
            c[1]++;
        }
    }

    public Map<Key, long[]> getCounts() {
        return counts;
    }
}
