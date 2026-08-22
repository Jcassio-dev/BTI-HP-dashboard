package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AprovacaoAggregatorTest {

    private static final String BTI = "92127264";

    @Test
    void dedupFiltrosEContagem() {
        Map<Long, TurmaInfo> turmas = Map.of(
                100L, new TurmaInfo(5L, "s1"),  // consolidada/graduacao (esta no mapa)
                300L, new TurmaInfo(7L, "s2"));

        AprovacaoAggregator agg = new AprovacaoAggregator(Set.of(BTI));

        // Aluno A aprovado, com 2 linhas (2 unidades) -> deve contar 1 so
        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        // Aluno B reprovado por falta
        agg.accumulate(turmas, new MatriculaRow(100, "B", BTI, "REPROVADO POR FALTAS"));
        // Aluno C nao e de BTI -> ignorado
        agg.accumulate(turmas, new MatriculaRow(100, "C", "2000005", "APROVADO"));
        // Aluno D em turma fora do mapa (nao consolidada/nao graduacao) -> ignorado
        agg.accumulate(turmas, new MatriculaRow(999, "D", BTI, "APROVADO"));
        // Aluno E trancou
        agg.accumulate(turmas, new MatriculaRow(100, "E", BTI, "TRANCADO"));

        Map<AprovacaoAggregator.Key, Desfechos> porTurma = agg.getDesfechos();

        assertEquals(1, porTurma.size());
        assertEquals(new Desfechos(1, 0, 1, 1), porTurma.get(new AprovacaoAggregator.Key(5L, "s1")));
        assertNull(porTurma.get(new AprovacaoAggregator.Key(7L, "s2")));

        assertEquals(new Desfechos(1, 0, 1, 1), agg.getDesfechosPorComponente().get(5L));
    }

    @Test
    void trancadosContamNoPardisciplinaProfessorEnaoNaTaxa() {
        Map<Long, TurmaInfo> turmas = Map.of(100L, new TurmaInfo(5L, "s1"));
        AprovacaoAggregator agg = new AprovacaoAggregator(Set.of(BTI));

        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "B", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "C", BTI, "REPROVADO POR MEDIA"));
        agg.accumulate(turmas, new MatriculaRow(100, "D", BTI, "TRANCADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "E", BTI, "CANCELADO"));

        Desfechos d = agg.getDesfechos().get(new AprovacaoAggregator.Key(5L, "s1"));

        assertEquals(new Desfechos(2, 1, 0, 2), d);
        assertEquals(3, d.totalAvaliados());
        assertEquals(5, d.totalMatriculados());
        assertEquals(2d / 3d, d.taxaAprovacao(), 1e-9);
    }

    @Test
    void componenteSomaOsProfessoresDaMesmaDisciplina() {
        Map<Long, TurmaInfo> turmas = Map.of(
                100L, new TurmaInfo(5L, "s1"),
                200L, new TurmaInfo(5L, "s2"));

        AprovacaoAggregator agg = new AprovacaoAggregator(Set.of(BTI));

        agg.accumulate(turmas, new MatriculaRow(100, "A", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(100, "B", BTI, "TRANCADO"));
        agg.accumulate(turmas, new MatriculaRow(200, "C", BTI, "APROVADO"));
        agg.accumulate(turmas, new MatriculaRow(200, "D", BTI, "REPROVADO POR FALTAS"));

        assertEquals(2, agg.getDesfechos().size());
        assertEquals(new Desfechos(2, 0, 1, 1), agg.getDesfechosPorComponente().get(5L));
    }
}
