package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndicesParserTest {

    // Estrutura fiel ao portal: indices como <acronym title="...">SIGLA</acronym> + <div>valor</div>,
    // dados institucionais como pares label/valor, e integralizacao com a barra de progresso.
    private static final String PORTAL = """
        <html><body>
        <a href="/sigaa/portais/discente/discente.jsf">m</a>
        <table>
          <caption>Dados Institucionais</caption>
          <tr><th>Matrícula:</th><td>20230055820</td></tr>
          <tr><th>Curso:</th><td>TECNOLOGIA DA INFORMAÇÃO/IMD - NATAL - BACHARELADO - N</td></tr>
          <tr><th>Status:</th><td>ATIVO</td></tr>
          <tr><th>Orientador Acadêmico:</th><td>ITAMIR DE MORAIS BARROCA FILHO</td></tr>
        </table>
        <table>
          <caption>Índices Acadêmicos</caption>
          <tr>
            <td><acronym title="Média de Conclusão">MC:</acronym></td><td><div>7.802</div></td>
            <td><acronym title="Índice de Rendimento Acadêmico">IRA:</acronym></td><td><div>6.7931</div></td>
          </tr>
          <tr>
            <td><acronym title="Índice de Eficiência Acadêmica">IEA:</acronym></td><td><div>5.4051</div></td>
          </tr>
        </table>
        <table>
          <caption>Integralizações</caption>
          <tr><th>CH Obrigatória Pendente</th><td>0</td></tr>
          <tr><th>CH Total Currículo</th><td>2600</td></tr>
          <tr><td>62% Integralizado</td></tr>
        </table>
        </body></html>
        """;

    @Test
    void pegaOsIndicesQueImportamComONomeCompleto() {
        IndicesParser.Indices ind = IndicesParser.de(PORTAL);

        assertEquals("6.7931", ind.valor("IRA"));
        assertEquals("7.802", ind.valor("MC"));
        assertEquals("Índice de Rendimento Acadêmico", ind.nome("IRA"));
    }

    @Test
    void indiceInexistenteVoltaVazio() {
        IndicesParser.Indices ind = IndicesParser.de(PORTAL);
        assertTrue(ind.valorOpt("NAOEXISTE").isEmpty());
    }

    @Test
    void pegaOrientadorEStatusDosDadosInstitucionais() {
        IndicesParser.Indices ind = IndicesParser.de(PORTAL);

        assertEquals("ITAMIR DE MORAIS BARROCA FILHO", ind.dado("Orientador Acadêmico"));
        assertEquals("ATIVO", ind.dado("Status"));
    }

    @Test
    void pegaOPercentualIntegralizado() {
        IndicesParser.Indices ind = IndicesParser.de(PORTAL);

        assertEquals(Optional.of(62), ind.percentualIntegralizado());
    }

    @Test
    void portalSemIndicesNaoQuebra() {
        IndicesParser.Indices ind = IndicesParser.de("<html><body>nada</body></html>");

        assertTrue(ind.valorOpt("IRA").isEmpty());
        assertTrue(ind.percentualIntegralizado().isEmpty());
    }
}
