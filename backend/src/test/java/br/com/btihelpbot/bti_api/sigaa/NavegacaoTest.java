package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavegacaoTest {

    // Trecho fiel ao portal: form do menu com id e ViewState, e o array JS com o item de notas.
    private static final String PORTAL = """
        <html><body>
        <form name="menu:form_menu_discente" method="post">
          <input type="hidden" name="id" value="863532"/>
          <input type="hidden" name="jscook_action"/>
          <input type="hidden" name="javax.faces.ViewState" value="j_id5"/>
        </form>
        <script>
          var m = ['Ensino', null, 'menu:form_menu_discente', null,
            ['<img/>', 'Consultar Minhas Notas', 'menu_form_menu_discente_discente_menu:A]#{ relatorioNotasAluno.gerarRelatorio }', 'menu:form_menu_discente', null]];
        </script>
        </body></html>
        """;

    @Test
    void montaOsCamposDoPostParaOItemDeMenu() {
        Map<String, String> campos = Navegacao.camposMenu(PORTAL, "Consultar Minhas Notas").orElseThrow();

        assertEquals("863532", campos.get("id"));
        assertEquals("j_id5", campos.get("javax.faces.ViewState"));
        assertEquals("menu_form_menu_discente_discente_menu:A]#{ relatorioNotasAluno.gerarRelatorio }",
                campos.get("jscook_action"));
        assertEquals("menu:form_menu_discente", campos.get("menu:form_menu_discente"));
    }

    @Test
    void itemInexistenteVoltaVazio() {
        assertTrue(Navegacao.camposMenu(PORTAL, "Tela Que Nao Existe").isEmpty());
    }

    @Test
    void semFormularioDeMenuVoltaVazio() {
        Optional<Map<String, String>> r = Navegacao.camposMenu("<html><body>nada</body></html>", "Consultar Minhas Notas");
        assertTrue(r.isEmpty());
    }
}
