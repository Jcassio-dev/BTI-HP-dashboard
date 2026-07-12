package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AprovacaoAggregatorTest {

    private static final String BTI = AprovacaoAggregator.CURSO_BTI;

    @Test
    void dedupFiltrosEContagem() {
        Map<Long, TurmaInfo> turmas = Map.of(
                100L, new TurmaInfo(5L, "s1"),  // consolidada/graduacao (esta no mapa)
                300L, new TurmaInfo(7L, "s2"));

        AprovacaoAggregator agg = new AprovacaoAggregator();

        // Aluno A aprovado, com 2 linhas (2 unidades) -> deve contar 1 so
        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        // Aluno B reprovado
        agg.accumulate(turmas, new MatriculaRow(100, "B", BTI, "REPROVADO POR FALTAS"));
        // Aluno C nao e de BTI -> ignorado
        agg.accumulate(turmas, new MatriculaRow(100, "C", "2000005", "APROVADO"));
        // Aluno D em turma fora do mapa (nao consolidada/nao graduacao) -> ignorado
        agg.accumulate(turmas, new MatriculaRow(999, "D", BTI, "APROVADO"));
        // Aluno E trancado -> situacao ignorada
        agg.accumulate(turmas, new MatriculaRow(100, "E", BTI, "TRANCADO"));

        Map<AprovacaoAggregator.Key, long[]> counts = agg.getCounts();

        // So o par (componente 5, docente s1): 1 aprovado (A), 1 reprovado (B)
        assertEquals(1, counts.size());
        assertArrayEquals(new long[]{1, 1}, counts.get(new AprovacaoAggregator.Key(5L, "s1")));
        assertNull(counts.get(new AprovacaoAggregator.Key(7L, "s2")));
    }
}
