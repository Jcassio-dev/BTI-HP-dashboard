package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColetorTest {

    private static final String PORTAL = """
        <html><body>
        <a href="/sigaa/portais/discente/discente.jsf">m</a>
        <table class="listagem">
          <thead><tr><th>Componente Curricular</th><th>Local</th><th>Horário</th></tr></thead>
          <tbody>
            <tr class="odd">
              <td class="descricao"><a>IMD0030 - ALGORITMOS</a></td>
              <td class="info">IMD - A104</td><td class="info">24M34</td>
            </tr>
          </tbody>
        </table>
        <table>
          <tr><td>Status:</td><td>ATIVO</td></tr>
          <tr>
            <td><acronym title="Índice de Rendimento Acadêmico">IRA:</acronym></td><td><div>6.7931</div></td>
          </tr>
        </table>
        <div>62% Integralizado</div>
        </body></html>
        """;

    private static final String LOGIN = "<html><body><input type=password></body></html>";

    private final Clock relogio = Clock.fixed(Instant.parse("2026-08-29T15:00:00Z"), ZoneOffset.UTC);

    @Test
    void colheTurmasEIndicesNumaUnicaBusca() {
        AtomicInteger buscas = new AtomicInteger();
        SigaaHttp http = (url, cookie) -> {
            buscas.incrementAndGet();
            return PORTAL;
        };
        Coletor coletor = new Coletor(http, new FilaSigaa(2, 10), relogio);

        DadosSigaa d = coletor.coletar("aluno@jid", "JSESSIONID=x");

        assertEquals(1, buscas.get(), "o portal ja traz turmas e indices, uma busca so");
        assertEquals(1, d.turmas().size());
        assertEquals("6.7931", d.indices().stream().filter(i -> i.sigla().equals("IRA")).findFirst().get().valor());
        assertEquals("ATIVO", d.institucional().get("Status"));
        assertEquals(62, d.integralizado());
        assertEquals(Instant.parse("2026-08-29T15:00:00Z"), d.atualizadoEm());
    }

    @Test
    void sessaoInvalidaNoMomentoDaColetaAvisa() {
        SigaaHttp http = (url, cookie) -> LOGIN;
        Coletor coletor = new Coletor(http, new FilaSigaa(2, 10), relogio);

        assertThrows(PrecisaConectar.class, () -> coletor.coletar("aluno@jid", "cookie"));
    }
}
