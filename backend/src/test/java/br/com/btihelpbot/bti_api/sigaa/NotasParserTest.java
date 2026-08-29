package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotasParserTest {

    // Uma tabela por periodo (no caption), colunas: Codigo, Disciplina, U1, U2, U3, Rec, Resultado, Faltas, Situacao.
    private static final String NOTAS = """
        <html><body>
        <table>
          <caption>2026.2</caption>
          <thead><tr><th>Código</th><th>Disciplina</th><th>Unidade 1</th><th>Unidade 2</th>
            <th>Unidade 3</th><th>Recuperação</th><th>Resultado</th><th>Faltas</th><th>Situação</th></tr></thead>
          <tbody>
            <tr><td>IMD0298</td><td>CCNA2 - SWITCHING</td><td></td><td></td><td></td><td></td><td>--</td><td>0</td><td>--</td></tr>
          </tbody>
        </table>
        <table>
          <caption>2026.1</caption>
          <thead><tr><th>Código</th><th>Disciplina</th><th>Unidade 1</th><th>Unidade 2</th>
            <th>Unidade 3</th><th>Recuperação</th><th>Resultado</th><th>Faltas</th><th>Situação</th></tr></thead>
          <tbody>
            <tr><td>IMD0024</td><td>CÁLCULO I</td><td>8.5</td><td>7.0</td><td>9.0</td><td></td><td>8.2</td><td>4</td><td>APROVADO POR NOTA</td></tr>
            <tr><td>IMD0034</td><td>FÍSICA</td><td>3.0</td><td>4.0</td><td>2.0</td><td>5.0</td><td>3.5</td><td>16</td><td>REPROVADO POR MÉDIA E POR FALTAS</td></tr>
          </tbody>
        </table>
        </body></html>
        """;

    @Test
    void agrupaAsNotasPorPeriodo() {
        List<NotasParser.Periodo> periodos = NotasParser.de(NOTAS);

        assertEquals(2, periodos.size());
        assertEquals("2026.2", periodos.get(0).periodo());
        assertEquals("2026.1", periodos.get(1).periodo());
    }

    @Test
    void leAsColunasDeUmaDisciplinaFechada() {
        NotasParser.Nota calc = NotasParser.de(NOTAS).get(1).notas().get(0);

        assertEquals("IMD0024", calc.codigo());
        assertEquals("Cálculo I", calc.disciplina());
        assertEquals(List.of("8.5", "7.0", "9.0"), calc.unidades());
        assertEquals("8.2", calc.resultado());
        assertEquals("4", calc.faltas());
        assertEquals("APROVADO POR NOTA", calc.situacao());
    }

    @Test
    void reconheceQuandoAsNotasAindaNaoForamLancadas() {
        NotasParser.Nota atual = NotasParser.de(NOTAS).get(0).notas().get(0);

        assertFalse(atual.temResultado(), "resultado -- nao conta como lancado");
        assertTrue(NotasParser.de(NOTAS).get(1).notas().get(0).temResultado());
    }

    @Test
    void paginaSemNotasNaoQuebra() {
        assertTrue(NotasParser.de("<html><body>nada</body></html>").isEmpty());
    }
}
