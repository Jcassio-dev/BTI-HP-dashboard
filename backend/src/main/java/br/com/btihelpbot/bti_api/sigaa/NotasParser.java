package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Le o boletim do SIGAA: uma tabela por periodo, cada linha uma disciplina com notas e situacao. */
public final class NotasParser {

    private static final Pattern PERIODO = Pattern.compile("^\\s*20\\d{2}\\.\\d\\s*$");
    private static final Pattern VAZIO = Pattern.compile("^[-\\s]*$");

    private NotasParser() {}

    public record Nota(
            String codigo, String disciplina, List<String> unidades,
            String recuperacao, String resultado, String faltas, String situacao) {

        public boolean temResultado() {
            return !VAZIO.matcher(resultado == null ? "" : resultado).matches();
        }
    }

    public record Periodo(String periodo, List<Nota> notas) {}

    public static List<Periodo> de(String html) {
        List<Periodo> out = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        for (Element tabela : doc.select("table")) {
            String periodo = periodo(tabela);
            if (periodo == null) {
                continue;
            }
            List<Nota> notas = new ArrayList<>();
            for (Element tr : tabela.select("tbody > tr")) {
                List<Element> tds = tr.select("> td");
                if (tds.size() < 9) {
                    continue;
                }
                notas.add(new Nota(
                        tds.get(0).text().trim(),
                        PortalParser.tituloCase(tds.get(1).text().trim()),
                        List.of(tds.get(2).text().trim(), tds.get(3).text().trim(), tds.get(4).text().trim()),
                        tds.get(5).text().trim(),
                        tds.get(6).text().trim(),
                        tds.get(7).text().trim(),
                        tds.get(8).text().trim()));
            }
            if (!notas.isEmpty()) {
                out.add(new Periodo(periodo, notas));
            }
        }
        return out;
    }

    private static String periodo(Element tabela) {
        Element caption = tabela.selectFirst("caption");
        if (caption != null && PERIODO.matcher(caption.text()).matches()) {
            return caption.text().trim();
        }
        return null;
    }
}
