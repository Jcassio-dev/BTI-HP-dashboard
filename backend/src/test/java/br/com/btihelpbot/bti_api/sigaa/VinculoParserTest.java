package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VinculoParserTest {

    private static final String ESCOLHA = """
        <html><body>
        <form action="https://sigaa.ufrn.br/sigaa/vinculos.jsf" method="post">
          <table class="listagem table tabela-selecao-vinculo">
            <caption>Ativos</caption>
            <tbody>
              <tr class="linhaPar selecionado">
                <td><a id="link" name="link"
                       href="https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&amp;vinculo=2"><img/></a></td>
                <td><a class="withoutFormat">Discente</a></td>
                <td><a class="withoutFormat">2019000000</a></td>
                <td><a class="withoutFormat">Curso: ADMINISTRAÇÃO/CCSA - NATAL - BACHARELADO</a></td>
              </tr>
            </tbody>
          </table>
          <table class="listagem table tabela-selecao-vinculo">
            <caption>Inativos</caption>
            <tbody>
              <tr class="linhaPar inativo ">
                <td><a id="linkInativo" name="linkInativo"
                       href="https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&amp;vinculo=1"><img/></a></td>
                <td><a class="withoutFormatInativo">Discente</a></td>
                <td><a class="withoutFormatInativo">2015000000</a></td>
                <td><a class="withoutFormatInativo">Curso: LETRAS/CCHLA - NATAL - LICENCIATURA</a></td>
              </tr>
            </tbody>
          </table>
        </form>
        </body></html>
        """;

    private static final String PORTAL = """
        <html><body><a href="/sigaa/portais/discente/discente.jsf">Portal</a></body></html>
        """;

    @Test
    void reconheceATelaDeEscolha() {
        assertTrue(VinculoParser.telaDeEscolha(ESCOLHA));
    }

    @Test
    void naoConfundePortalComTelaDeEscolha() {
        assertFalse(VinculoParser.telaDeEscolha(PORTAL));
    }

    @Test
    void pegaOLinkDoVinculoDiscenteAtivo() {
        Optional<String> link = VinculoParser.linkDiscenteAtivo(ESCOLHA);

        assertTrue(link.isPresent());
        assertEquals("https://sigaa.ufrn.br/sigaa/escolhaVinculo.do?dispatch=escolher&vinculo=2", link.get());
    }

    @Test
    void ignoraVinculoInativo() {
        Optional<String> link = VinculoParser.linkDiscenteAtivo(ESCOLHA);

        assertFalse(link.get().contains("vinculo=1"));
    }
}
