package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VinculoServiceTest {

    private Instant agora = Instant.parse("2026-08-23T12:00:00Z");
    private final Clock relogio = new Clock() {
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId z) { return this; }
        public Instant instant() { return agora; }
    };

    private VinculoService servico() {
        return new VinculoService(relogio, Duration.ofMinutes(10));
    }

    @Test
    void oTokenDevolveOAlunoQueOPediu() {
        VinculoService s = servico();

        String token = s.gerar("5584999@s.whatsapp.net");

        assertEquals(Optional.of("5584999@s.whatsapp.net"), s.consumir(token));
    }

    @Test
    void oTokenSoServeUmaVez() {
        VinculoService s = servico();
        String token = s.gerar("aluno@jid");

        s.consumir(token);

        assertTrue(s.consumir(token).isEmpty());
    }

    @Test
    void oTokenExpira() {
        VinculoService s = servico();
        String token = s.gerar("aluno@jid");

        agora = agora.plus(Duration.ofMinutes(11));

        assertTrue(s.consumir(token).isEmpty());
    }

    @Test
    void tokenDesconhecidoNaoServe() {
        assertTrue(servico().consumir("nao-existe").isEmpty());
    }

    @Test
    void pedirDeNovoInvalidaOTokenAnterior() {
        VinculoService s = servico();
        String primeiro = s.gerar("aluno@jid");

        String segundo = s.gerar("aluno@jid");

        assertNotEquals(primeiro, segundo);
        assertTrue(s.consumir(primeiro).isEmpty());
        assertEquals(Optional.of("aluno@jid"), s.consumir(segundo));
    }

    @Test
    void doisAlunosRecebemTokensDiferentes() {
        VinculoService s = servico();

        assertNotEquals(s.gerar("aluno1@jid"), s.gerar("aluno2@jid"));
    }

    @Test
    void oTokenNaoCarregaOJidDentro() {
        String token = servico().gerar("5584999@s.whatsapp.net");

        assertTrue(token.length() >= 22);
        assertTrue(!token.contains("5584999"));
    }
}
