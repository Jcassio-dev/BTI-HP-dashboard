package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DesfechosTest {

    @Test
    void trancadosFicamForaDaTaxaEDentroDoTotal() {
        // MAT0031: 38 aprovados, 4 reprovados por nota, 0 por falta, 10 trancados
        Desfechos d = new Desfechos(38, 4, 0, 10);

        assertEquals(42, d.totalAvaliados());
        assertEquals(52, d.totalMatriculados());
        assertEquals(38d / 42d, d.taxaAprovacao(), 1e-9);
    }

    @Test
    void reprovadoPorFaltaContaNaTaxaComoReprovado() {
        Desfechos d = new Desfechos(165, 7, 2, 22);

        assertEquals(174, d.totalAvaliados());
        assertEquals(196, d.totalMatriculados());
        assertEquals(165d / 174d, d.taxaAprovacao(), 1e-9);
    }

    @Test
    void turmaSoComTrancadosNaoTemTaxa() {
        Desfechos d = new Desfechos(0, 0, 0, 7);

        assertEquals(0, d.totalAvaliados());
        assertEquals(7, d.totalMatriculados());
        assertEquals(0d, d.taxaAprovacao(), 1e-9);
    }

    @Test
    void somaAcumulaCategoriaPorCategoria() {
        Desfechos a = new Desfechos(10, 2, 1, 3);
        Desfechos b = new Desfechos(5, 1, 0, 4);

        assertEquals(new Desfechos(15, 3, 1, 7), a.mais(b));
    }
}
