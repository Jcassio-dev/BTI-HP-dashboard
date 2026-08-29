package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reconstroi o POST que o menu JSF do SIGAA dispara ao clicar num item (ex: "Consultar Minhas
 * Notas"). Tudo sai da propria pagina do portal: o form do menu (id, ViewState) e o array de
 * JavaScript que liga cada rotulo a sua acao. Nada e hardcodado por sessao.
 */
public final class Navegacao {

    private static final String FORM_MENU = "menu:form_menu_discente";

    private Navegacao() {}

    public static Optional<Map<String, String>> camposMenu(String portalHtml, String rotulo) {
        Document doc = Jsoup.parse(portalHtml);
        Element form = doc.selectFirst("form[name=" + FORM_MENU + "]");
        if (form == null) {
            return Optional.empty();
        }

        String acao = acaoDoItem(portalHtml, rotulo);
        if (acao == null) {
            return Optional.empty();
        }

        Map<String, String> campos = new LinkedHashMap<>();
        campos.put(FORM_MENU, FORM_MENU);
        campos.put("id", valorInput(form, "id"));
        campos.put("jscook_action", acao);
        campos.put("javax.faces.ViewState", valorInput(form, "javax.faces.ViewState"));
        return Optional.of(campos);
    }

    /** No array JS o item vem como [icone, 'Rotulo', 'ACAO', ...]; pega a acao depois do rotulo. */
    private static String acaoDoItem(String html, String rotulo) {
        Pattern p = Pattern.compile("'" + Pattern.quote(rotulo) + "'\\s*,\\s*'([^']*)'");
        Matcher m = p.matcher(html);
        return m.find() ? m.group(1) : null;
    }

    private static String valorInput(Element form, String nome) {
        Element in = form.selectFirst("input[name=\"" + nome + "\"]");
        return in == null ? "" : in.attr("value");
    }
}
