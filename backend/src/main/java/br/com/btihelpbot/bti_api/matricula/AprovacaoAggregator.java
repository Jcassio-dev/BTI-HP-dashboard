package br.com.btihelpbot.bti_api.matricula;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AprovacaoAggregator {

    public record Key(long componenteId, String siape) {}

    private final Set<String> cursos;
    private final Map<Key, long[]> counts = new HashMap<>();
    private final Map<Long, long[]> breakdown = new HashMap<>();
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

        breakdown.computeIfAbsent(turma.idComponente(), k -> new long[4])[cat.ordinal()]++;

        if (cat == SituacaoClassifier.Categoria.APROVADO) {
            counts.computeIfAbsent(new Key(turma.idComponente(), turma.siape()), k -> new long[2])[0]++;
        } else if (cat == SituacaoClassifier.Categoria.REPROVADO_NOTA
                || cat == SituacaoClassifier.Categoria.REPROVADO_FALTA) {
            counts.computeIfAbsent(new Key(turma.idComponente(), turma.siape()), k -> new long[2])[1]++;
        }
    }

    public Map<Key, long[]> getCounts() {
        return counts;
    }

    public Map<Long, long[]> getBreakdown() {
        return breakdown;
    }
}
