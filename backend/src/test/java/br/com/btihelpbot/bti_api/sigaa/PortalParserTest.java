package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalParserTest {

    // Estrutura fiel ao portal do SIGAA (bloco "Turmas do Semestre"), com conteudo ficticio.
    private static final String PORTAL = """
        <html><body>
        <div id="portal-discente">
          <table class="listagem">
            <caption>Turmas do Semestre</caption>
            <thead><tr><th>Componente Curricular</th><th>Local</th><th>Horário</th><th></th></tr></thead>
            <tbody>
              <tr><td>1</td></tr>
              <tr class="odd">
                <td class="descricao">
                  <form id="form_acessarTurmaVirtual" method="post">
                    <a href="#">IMD0030 - ALGORITMOS E ESTRUTURAS DE DADOS I</a>
                    <input name="javax.faces.ViewState" value="x" type="hidden"/>
                  </form>
                </td>
                <td class="info">IMD - A104</td>
                <td class="info"><center>24M34</center></td>
                <td></td>
              </tr>
              <tr class="even">
                <td class="descricao">
                  <form id="form_acessarTurmaVirtualj_id_1" method="post">
                    <a href="#">LEVANTAMENTO E MODELAGEM DE REQUISITOS</a>
                  </form>
                </td>
                <td class="info">IMD - A305</td>
                <td class="info"><center>35T12</center></td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </div>
        </body></html>
        """;

    private static final String LOGIN = """
        <html><body>
          <form id="login-form" action="/sso-server/login" method="post">
            <input name="username" type="text"/>
            <input name="password" type="password"/>
          </form>
        </body></html>
        """;

    @Test
    void extraiAsTurmasDoSemestre() {
        List<PortalParser.Turma> turmas = PortalParser.turmas(PORTAL);

        assertEquals(2, turmas.size());
        assertEquals("IMD0030", turmas.get(0).codigo());
        assertEquals("Algoritmos e Estruturas de Dados I", turmas.get(0).nome());
        assertEquals("IMD - A104", turmas.get(0).local());
        assertEquals("24M34", turmas.get(0).horario());
    }

    @Test
    void semCodigoNoNomeMantemONomeInteiroECodigoVazio() {
        List<PortalParser.Turma> turmas = PortalParser.turmas(PORTAL);

        assertEquals("", turmas.get(1).codigo());
        assertEquals("Levantamento e Modelagem de Requisitos", turmas.get(1).nome());
        assertEquals("35T12", turmas.get(1).horario());
    }

    @Test
    void reconheceUmaPaginaAutenticada() {
        assertTrue(PortalParser.autenticado(PORTAL));
        assertFalse(PortalParser.autenticado(LOGIN));
    }

    @Test
    void paginaDeLoginNaoTemTurmas() {
        assertTrue(PortalParser.turmas(LOGIN).isEmpty());
    }

    @Test
    void horarioLegivelTraduzOCodigoDoSigaa() {
        // 24M34 = segunda e quarta, manha, 3o e 4o horario
        assertEquals("Seg e Qua, manhã (3º-4º)", PortalParser.horarioLegivel("24M34"));
        // 35T12 = terca e quinta, tarde, 1o e 2o
        assertEquals("Ter e Qui, tarde (1º-2º)", PortalParser.horarioLegivel("35T12"));
        // horario que nao casa o padrao volta como veio
        assertEquals("EAD", PortalParser.horarioLegivel("EAD"));
    }
}
