package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorLoginTest {

    private final AtomicReference<Instant> agora = new AtomicReference<>(Instant.parse("2026-08-29T12:00:00Z"));

    private LimitadorLogin novo(int max, Duration janela) {
        Clock r = new Clock() {
            public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId z) { return this; }
            public Instant instant() { return agora.get(); }
        };
        return new LimitadorLogin(r, max, janela);
    }

    @Test
    void liberaDentroDoLimite() {
        LimitadorLogin lim = novo(3, Duration.ofMinutes(1));

        assertTrue(lim.permitir("1.1.1.1"));
        assertTrue(lim.permitir("1.1.1.1"));
        assertTrue(lim.permitir("1.1.1.1"));
    }

    @Test
    void bloqueiaAcimaDoLimite() {
        LimitadorLogin lim = novo(3, Duration.ofMinutes(1));

        lim.permitir("1.1.1.1");
        lim.permitir("1.1.1.1");
        lim.permitir("1.1.1.1");

        assertFalse(lim.permitir("1.1.1.1"));
    }

    @Test
    void ipsDiferentesNaoSeAfetam() {
        LimitadorLogin lim = novo(1, Duration.ofMinutes(1));

        assertTrue(lim.permitir("1.1.1.1"));
        assertTrue(lim.permitir("2.2.2.2"));
        assertFalse(lim.permitir("1.1.1.1"));
    }

    @Test
    void aJanelaExpiraELibera() {
        LimitadorLogin lim = novo(1, Duration.ofMinutes(1));

        assertTrue(lim.permitir("1.1.1.1"));
        assertFalse(lim.permitir("1.1.1.1"));

        agora.set(Instant.parse("2026-08-29T12:01:01Z"));

        assertTrue(lim.permitir("1.1.1.1"));
    }
}
