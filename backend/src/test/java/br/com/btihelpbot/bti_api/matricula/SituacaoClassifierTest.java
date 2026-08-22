package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import static br.com.btihelpbot.bti_api.matricula.SituacaoClassifier.Categoria;
import static br.com.btihelpbot.bti_api.matricula.SituacaoClassifier.classificar;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoClassifierTest {

    @Test
    void aprovados() {
        assertEquals(Categoria.APROVADO, classificar("APROVADO"));
        assertEquals(Categoria.APROVADO, classificar("APROVADO POR NOTA"));
        assertEquals(Categoria.APROVADO, classificar("CUMPRIU"));
        assertEquals(Categoria.APROVADO, classificar(" aprovado ")); // trim + case
    }

    @Test
    void reprovadoPorFaltaSeparadoDeReprovadoPorNota() {
        assertEquals(Categoria.REPROVADO_FALTA, classificar("REPROVADO POR FALTAS"));
        assertEquals(Categoria.REPROVADO_NOTA, classificar("REPROVADO"));
        assertEquals(Categoria.REPROVADO_NOTA, classificar("REPROVADO POR MÉDIA E POR FALTAS"));
        assertEquals(Categoria.REPROVADO_NOTA, classificar("REPROVADO POR NOTA E FALTA"));
    }

    @Test
    void trancados() {
        assertEquals(Categoria.TRANCADO, classificar("TRANCADO"));
        assertEquals(Categoria.TRANCADO, classificar("CANCELADO"));
        assertEquals(Categoria.TRANCADO, classificar("DESISTENCIA"));
    }

    @Test
    void ignorados() {
        assertEquals(Categoria.IGNORADO, classificar("INDEFERIDO"));
        assertEquals(Categoria.IGNORADO, classificar("DISPENSADO"));
        assertEquals(Categoria.IGNORADO, classificar("MATRICULADO"));
        assertEquals(Categoria.IGNORADO, classificar(null));
        assertEquals(Categoria.IGNORADO, classificar(""));
    }
}
