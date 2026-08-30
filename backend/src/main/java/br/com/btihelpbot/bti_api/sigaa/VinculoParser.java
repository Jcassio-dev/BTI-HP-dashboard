package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

/**
 * Le a tela "Escolha seu vinculo" do SIGAA, que aparece quando o aluno tem mais de um vinculo.
 * O login cai aqui em vez do portal; e preciso escolher o vinculo Discente ativo pra seguir.
 */
public final class VinculoParser {

    private VinculoParser() {}

    public static boolean telaDeEscolha(String html) {
        return html != null && html.contains("escolhaVinculo.do");
    }

    public static Optional<String> linkDiscenteAtivo(String html) {
        Document doc = Jsoup.parse(html);
        for (Element tr : doc.select("tr")) {
            if (tr.hasClass("inativo")) {
                continue;
            }
            Element link = tr.selectFirst("a[href*=escolhaVinculo.do]");
            if (link == null) {
                continue;
            }
            if (!tr.text().contains("Discente")) {
                continue;
            }
            return Optional.of(link.attr("href"));
        }
        return Optional.empty();
    }
}
