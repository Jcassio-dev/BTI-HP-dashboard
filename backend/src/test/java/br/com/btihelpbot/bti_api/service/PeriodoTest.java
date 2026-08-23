package br.com.btihelpbot.bti_api.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoTest {

    @Test
    void periodoZeroOuNegativoCobreTudo() {
        assertEquals(Instant.EPOCH, MetricsService.corteDe(0));
        assertEquals(Instant.EPOCH, MetricsService.corteDe(-5));
    }

    @Test
    void periodoPositivoRecuaOsDiasPedidos() {
        Instant corte = MetricsService.corteDe(30);
        long dias = ChronoUnit.DAYS.between(corte, Instant.now());

        assertEquals(30, dias);
        assertTrue(corte.isBefore(Instant.now()));
    }

    @Test
    void deslocamentoAndaAJanelaParaTras() {
        MetricsService.Janela atual = MetricsService.janela(30, 0);
        MetricsService.Janela anterior = MetricsService.janela(30, 30);

        assertEquals(30, ChronoUnit.DAYS.between(atual.desde(), atual.ate()));
        assertEquals(30, ChronoUnit.DAYS.between(anterior.desde(), anterior.ate()));
        // as janelas se encostam; a folga e so o relogio avancando entre as duas chamadas
        assertTrue(Math.abs(ChronoUnit.SECONDS.between(anterior.ate(), atual.desde())) <= 1);
    }

    @Test
    void janelaSemDiasCobreTudoAteAgora() {
        MetricsService.Janela j = MetricsService.janela(0, 0);

        assertEquals(Instant.EPOCH, j.desde());
        assertTrue(j.ate().isAfter(Instant.now().minusSeconds(5)));
    }
}
