package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColetorTest {

    private static final String PORTAL = """
        <html><body>
        <a href="/sigaa/portais/discente/discente.jsf">m</a>
        <form name="menu:form_menu_discente">
          <input type="hidden" name="id" value="863532"/>
          <input type="hidden" name="javax.faces.ViewState" value="j_id5"/>
        </form>
        <script>['<img/>', 'Consultar Minhas Notas', 'acao_notas', 'menu:form_menu_discente', null];</script>
        <table class="listagem">
          <thead><tr><th>Componente Curricular</th><th>Local</th><th>Horário</th></tr></thead>
          <tbody>
            <tr class="odd">
              <td class="descricao"><a>IMD0030 - ALGORITMOS</a></td>
              <td class="info">IMD - A104</td><td class="info">24M34</td>
            </tr>
          </tbody>
        </table>
        <table><tr><td><acronym title="Índice de Rendimento Acadêmico">IRA:</acronym></td><td><div>6.7931</div></td></tr></table>
        <div>62% Integralizado</div>
        </body></html>
        """;

    private static final String NOTAS = """
        <html><body>
        <table><caption>2026.1</caption>
          <tbody><tr><td>IMD0024</td><td>CÁLCULO I</td><td>8.5</td><td>7.0</td><td>9.0</td><td></td><td>8.2</td><td>4</td><td>APROVADO</td></tr></tbody>
        </table></body></html>
        """;

    private static final String LOGIN = "<html><body><input type=password></body></html>";

    private static final String ESCOLHA = """
        <html><body>
        <table class="tabela-selecao-vinculo">
          <tr class="selecionado">
            <td><a href="https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&amp;vinculo=2">x</a></td>
            <td>Discente</td>
          </tr>
          <tr class="inativo">
            <td><a href="https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&amp;vinculo=1">y</a></td>
            <td>Discente</td>
          </tr>
        </table></body></html>
        """;

    private final Clock relogio = Clock.fixed(Instant.parse("2026-08-29T15:00:00Z"), ZoneOffset.UTC);

    private static class NavFake implements Navegador {
        String portal = PORTAL;
        String telaInicial = null;
        boolean escolheu = false;
        String linkVinculoUsado;
        final AtomicInteger gets = new AtomicInteger();
        final AtomicInteger posts = new AtomicInteger();
        Map<String, String> ultimoPost;

        public String get(String url) {
            gets.incrementAndGet();
            if (url.contains("escolhaVinculo.do")) {
                escolheu = true;
                linkVinculoUsado = url;
                return "<html>ok</html>";
            }
            if (telaInicial != null && !escolheu) {
                return telaInicial;
            }
            return portal;
        }

        public String postForm(String url, Map<String, String> campos) {
            posts.incrementAndGet();
            ultimoPost = campos;
            return NOTAS;
        }

        public Optional<String> cookie(String nome) {
            return Optional.empty();
        }
    }

    @Test
    void colheTurmasIndicesENotas() {
        NavFake nav = new NavFake();
        Coletor coletor = new Coletor(new FilaSigaa(2, 10), relogio);

        DadosSigaa d = coletor.coletar("aluno@jid", nav);

        assertEquals(1, nav.gets.get(), "um GET no portal");
        assertEquals(1, nav.posts.get(), "um POST para as notas");
        assertEquals(1, d.turmas().size());
        assertEquals("6.7931", d.indices().stream().filter(i -> i.sigla().equals("IRA")).findFirst().get().valor());
        assertEquals(62, d.integralizado());
        assertEquals(1, d.boletim().size());
        assertEquals("2026.1", d.boletim().get(0).periodo());
    }

    @Test
    void oPostDeNotasLevaOsCamposCertos() {
        NavFake nav = new NavFake();
        new Coletor(new FilaSigaa(2, 10), relogio).coletar("aluno@jid", nav);

        assertEquals("863532", nav.ultimoPost.get("id"));
        assertEquals("acao_notas", nav.ultimoPost.get("jscook_action"));
        assertEquals("j_id5", nav.ultimoPost.get("javax.faces.ViewState"));
    }

    @Test
    void escolheVinculoDiscenteAtivoQuandoCaiNaTela() {
        NavFake nav = new NavFake();
        nav.telaInicial = ESCOLHA;
        Coletor coletor = new Coletor(new FilaSigaa(2, 10), relogio);

        DadosSigaa d = coletor.coletar("aluno@jid", nav);

        assertEquals("https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&vinculo=2",
                nav.linkVinculoUsado);
        assertEquals(1, d.turmas().size());
    }

    @Test
    void sessaoInvalidaNoMomentoDaColetaAvisa() {
        NavFake nav = new NavFake();
        nav.portal = LOGIN;

        assertThrows(PrecisaConectar.class,
                () -> new Coletor(new FilaSigaa(2, 10), relogio).coletar("aluno@jid", nav));
    }
}
