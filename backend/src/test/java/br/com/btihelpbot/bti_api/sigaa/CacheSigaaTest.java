package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheSigaaTest {

    private Instant agora = Instant.parse("2026-08-23T12:00:00Z");
    private final Clock relogio = new Clock() {
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(java.time.ZoneId z) { return this; }
        public Instant instant() { return agora; }
    };

    @Test
    void devolveOQueFoiGuardadoDentroDoPrazo() {
        CacheSigaa cache = new CacheSigaa(relogio);
        cache.guardar("aluno1", "turmas", "conteudo", Duration.ofMinutes(30));

        agora = agora.plus(Duration.ofMinutes(29));

        assertEquals(Optional.of("conteudo"), cache.obter("aluno1", "turmas", String.class));
    }

    @Test
    void expiraDepoisDoPrazo() {
        CacheSigaa cache = new CacheSigaa(relogio);
        cache.guardar("aluno1", "turmas", "conteudo", Duration.ofMinutes(30));

        agora = agora.plus(Duration.ofMinutes(31));

        assertTrue(cache.obter("aluno1", "turmas", String.class).isEmpty());
    }

    @Test
    void umAlunoNuncaVeOCacheDoOutro() {
        CacheSigaa cache = new CacheSigaa(relogio);
        cache.guardar("aluno1", "notas", "notas do aluno 1", Duration.ofMinutes(30));

        assertTrue(cache.obter("aluno2", "notas", String.class).isEmpty());
    }

    @Test
    void recursosDiferentesNaoSeMisturam() {
        CacheSigaa cache = new CacheSigaa(relogio);
        cache.guardar("aluno1", "turmas", "as turmas", Duration.ofMinutes(30));

        assertTrue(cache.obter("aluno1", "notas", String.class).isEmpty());
    }

    @Test
    void invalidarApagaTudoDoAlunoESoDele() {
        CacheSigaa cache = new CacheSigaa(relogio);
        cache.guardar("aluno1", "turmas", "a", Duration.ofMinutes(30));
        cache.guardar("aluno1", "notas", "b", Duration.ofMinutes(30));
        cache.guardar("aluno2", "turmas", "c", Duration.ofMinutes(30));

        cache.invalidar("aluno1");

        assertTrue(cache.obter("aluno1", "turmas", String.class).isEmpty());
        assertTrue(cache.obter("aluno1", "notas", String.class).isEmpty());
        assertEquals(Optional.of("c"), cache.obter("aluno2", "turmas", String.class));
    }
}
