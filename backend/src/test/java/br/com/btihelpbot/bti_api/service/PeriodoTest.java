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
}
