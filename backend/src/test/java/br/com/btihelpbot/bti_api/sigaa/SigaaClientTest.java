package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SigaaClientTest {

    private static final String PORTAL = """
        <html><body><input type="text"/>
          <a href="/sigaa/portais/discente/discente.jsf">m</a>
          <table class="listagem">
            <thead><tr><th>Componente Curricular</th><th>Local</th><th>Horário</th></tr></thead>
            <tbody>
              <tr class="odd">
                <td class="descricao"><a href="#">IMD0030 - ALGORITMOS E ESTRUTURAS DE DADOS I</a></td>
                <td class="info">IMD - A104</td><td class="info">24M34</td>
              </tr>
            </tbody>
          </table>
        </body></html>
        """;

    private static final String LOGIN = """
        <html><body><form><input name="username"/><input type="password" name="password"/></form></body></html>
        """;

    private Instant agora = Instant.parse("2026-08-23T12:00:00Z");
    private final Clock relogio = new Clock() {
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId z) { return this; }
        public Instant instant() { return agora; }
    };

    private Map<String, SessaoSigaa> banco;
    private SessaoService sessoes;
    private CacheSigaa cache;
    private AtomicInteger buscas;
    private String respostaHttp;
    private SigaaClient cliente;

    @BeforeEach
    void preparar() {
        banco = new HashMap<>();
        sessoes = new SessaoService(new SessaoSigaaRepositoryFake(banco),
                new CofreSessao(CofreSessao.gerarChave()), relogio, Duration.ofHours(6));
        cache = new CacheSigaa(relogio);
        buscas = new AtomicInteger();
        respostaHttp = PORTAL;
        SigaaHttp http = (url, cookie) -> {
            buscas.incrementAndGet();
            return respostaHttp;
        };
        cliente = new SigaaClient(http, sessoes, cache, new FilaSigaa(2, 50));
    }

    @Test
    void semSessaoPedeParaConectar() {
        assertThrows(SigaaClient.PrecisaConectar.class, () -> cliente.turmas("aluno@jid"));
    }

    @Test
    void comSessaoDevolveAsTurmas() {
        sessoes.salvar("aluno@jid", "JSESSIONID=vivo");

        List<PortalParser.Turma> turmas = cliente.turmas("aluno@jid");

        assertEquals(1, turmas.size());
        assertEquals("IMD0030", turmas.get(0).codigo());
    }

    @Test
    void aSegundaConsultaVemDoCacheSemBaterNoSigaa() {
        sessoes.salvar("aluno@jid", "JSESSIONID=vivo");

        cliente.turmas("aluno@jid");
        cliente.turmas("aluno@jid");

        assertEquals(1, buscas.get());
    }

    @Test
    void atualizarForcaUmaNovaBusca() {
        sessoes.salvar("aluno@jid", "JSESSIONID=vivo");

        cliente.turmas("aluno@jid");
        cliente.atualizar("aluno@jid");
        cliente.turmas("aluno@jid");

        assertEquals(2, buscas.get());
    }

    @Test
    void sessaoMortaNoMeioDoCaminhoLimpaTudoEPedeConexao() {
        sessoes.salvar("aluno@jid", "JSESSIONID=expirou");
        respostaHttp = LOGIN; // o SIGAA respondeu a tela de login

        assertThrows(SigaaClient.PrecisaConectar.class, () -> cliente.turmas("aluno@jid"));
        assertTrue(sessoes.cookieDe("aluno@jid").isEmpty(), "a sessao invalida devia ser apagada");
    }

    @Test
    void umAlunoNaoVeAsTurmasDoOutro() {
        sessoes.salvar("a@jid", "JSESSIONID=a");
        respostaHttp = PORTAL;
        cliente.turmas("a@jid");

        sessoes.salvar("b@jid", "JSESSIONID=b");
        cliente.turmas("b@jid");

        assertEquals(2, buscas.get(), "cada aluno tem seu proprio cache");
    }
}
