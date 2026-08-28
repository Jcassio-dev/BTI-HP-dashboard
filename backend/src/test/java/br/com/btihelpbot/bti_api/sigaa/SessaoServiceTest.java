package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessaoServiceTest {

    private Instant agora = Instant.parse("2026-08-23T12:00:00Z");
    private final Clock relogio = new Clock() {
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId z) { return this; }
        public Instant instant() { return agora; }
    };

    private Map<String, SessaoSigaa> banco;
    private SessaoService servico;

    @BeforeEach
    void preparar() {
        banco = new HashMap<>();
        SessaoSigaaRepository repo = new SessaoSigaaRepositoryFake(banco);
        CofreSessao cofre = new CofreSessao(CofreSessao.gerarChave());
        servico = new SessaoService(repo, cofre, relogio, Duration.ofHours(6));
    }

    @Test
    void guardaEDevolveOCookie() {
        servico.salvar("aluno@jid", "JSESSIONID=abc123");

        assertEquals(Optional.of("JSESSIONID=abc123"), servico.cookieDe("aluno@jid"));
    }

    @Test
    void oCookieNaoFicaEmClaroNoBanco() {
        servico.salvar("aluno@jid", "JSESSIONID=segredo");

        assertFalse(banco.get("aluno@jid").getCookieCifrado().contains("segredo"));
    }

    @Test
    void naoDevolveSessaoExpirada() {
        servico.salvar("aluno@jid", "JSESSIONID=abc");

        agora = agora.plus(Duration.ofHours(7));

        assertTrue(servico.cookieDe("aluno@jid").isEmpty());
    }

    @Test
    void temSessaoDizSeVale() {
        assertFalse(servico.temSessao("aluno@jid"));

        servico.salvar("aluno@jid", "JSESSIONID=abc");
        assertTrue(servico.temSessao("aluno@jid"));

        agora = agora.plus(Duration.ofHours(7));
        assertFalse(servico.temSessao("aluno@jid"));
    }

    @Test
    void esquecerApagaASessao() {
        servico.salvar("aluno@jid", "JSESSIONID=abc");

        servico.esquecer("aluno@jid");

        assertTrue(servico.cookieDe("aluno@jid").isEmpty());
        assertFalse(banco.containsKey("aluno@jid"));
    }

    @Test
    void reconectarSubstituiOCookieAntigo() {
        servico.salvar("aluno@jid", "JSESSIONID=velho");
        servico.salvar("aluno@jid", "JSESSIONID=novo");

        assertEquals(Optional.of("JSESSIONID=novo"), servico.cookieDe("aluno@jid"));
        assertEquals(1, banco.size());
    }
}
