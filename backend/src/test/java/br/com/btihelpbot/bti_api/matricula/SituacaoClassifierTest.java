package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import static br.com.btihelpbot.bti_api.matricula.SituacaoClassifier.Bucket;
import static br.com.btihelpbot.bti_api.matricula.SituacaoClassifier.classify;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoClassifierTest {

    @Test
    void aprovados() {
        assertEquals(Bucket.APROVADO, classify("APROVADO"));
        assertEquals(Bucket.APROVADO, classify("APROVADO POR NOTA"));
        assertEquals(Bucket.APROVADO, classify("CUMPRIU"));
        assertEquals(Bucket.APROVADO, classify(" aprovado ")); // trim + case
    }

    @Test
    void reprovados() {
        assertEquals(Bucket.REPROVADO, classify("REPROVADO"));
        assertEquals(Bucket.REPROVADO, classify("REPROVADO POR MÉDIA E POR FALTAS"));
        assertEquals(Bucket.REPROVADO, classify("REPROVADO POR FALTAS"));
        assertEquals(Bucket.REPROVADO, classify("REPROVADO POR NOTA E FALTA"));
    }

    @Test
    void ignorados() {
        assertEquals(Bucket.IGNORADO, classify("TRANCADO"));
        assertEquals(Bucket.IGNORADO, classify("INDEFERIDO"));
        assertEquals(Bucket.IGNORADO, classify("CANCELADO"));
        assertEquals(Bucket.IGNORADO, classify("DISPENSADO"));
        assertEquals(Bucket.IGNORADO, classify("MATRICULADO"));
        assertEquals(Bucket.IGNORADO, classify(null));
        assertEquals(Bucket.IGNORADO, classify(""));
    }
}
