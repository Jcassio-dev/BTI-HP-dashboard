package br.com.btihelpbot.bti_api.matricula;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Acumula aprovacoes/reprovacoes por (componente, docente), so pra alunos de BTI.
 *
 * Trata as duas armadilhas dos dados:
 *  - matriculas tem UMA LINHA POR UNIDADE -> dedup por (idTurma, discente).
 *  - so conta matriculas cuja turma esta no mapa (CONSOLIDADA + GRADUACAO).
 *
 * Stateful: chame accumulate() semestre a semestre e no fim leia getCounts().
 * idTurma e globalmente unico, entao o dedup vale entre semestres tambem.
 */
public class AprovacaoAggregator {

    /** id_curso do BTI no dados.ufrn.br. */
    public static final String CURSO_BTI = "92127264";

    public record Key(long componenteId, String siape) {}

    /** [aprovados, reprovados] */
    private final Map<Key, long[]> counts = new HashMap<>();
    private final Set<String> seen = new HashSet<>();

    public void accumulate(Map<Long, TurmaInfo> turmasDoSemestre, MatriculaRow row) {
        if (!CURSO_BTI.equals(row.idCurso())) {
            return;
        }
        String dedupKey = row.idTurma() + ":" + row.discente();
        if (!seen.add(dedupKey)) {
            return; // ja contamos essa matricula (outra unidade)
        }
        TurmaInfo turma = turmasDoSemestre.get(row.idTurma());
        if (turma == null) {
            return; // turma nao consolidada / nao graduacao / outro semestre
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
