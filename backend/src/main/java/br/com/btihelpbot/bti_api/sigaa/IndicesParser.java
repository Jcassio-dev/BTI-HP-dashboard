package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Le os indices academicos, dados institucionais e integralizacao do portal do discente. */
public final class IndicesParser {

    private static final Pattern PCT = Pattern.compile("(\\d{1,3})%\\s*Integr", Pattern.CASE_INSENSITIVE);

    private IndicesParser() {}

    /** Um indice pela sigla: valor e nome completo (do title do acronym). */
    public record Indice(String valor, String nome) {}

    public static final class Indices {
        private final Map<String, Indice> porSigla;
        private final Map<String, String> dados;
        private final Integer percentual;

        Indices(Map<String, Indice> porSigla, Map<String, String> dados, Integer percentual) {
            this.porSigla = porSigla;
            this.dados = dados;
            this.percentual = percentual;
        }

        public Optional<String> valorOpt(String sigla) {
            Indice i = porSigla.get(sigla.toUpperCase());
            return i == null ? Optional.empty() : Optional.of(i.valor());
        }

        public String valor(String sigla) {
            return valorOpt(sigla).orElse("");
        }

        public String nome(String sigla) {
            Indice i = porSigla.get(sigla.toUpperCase());
            return i == null ? "" : i.nome();
        }

        public String dado(String rotulo) {
            return dados.getOrDefault(rotulo, "");
        }

        public Optional<Integer> percentualIntegralizado() {
            return Optional.ofNullable(percentual);
        }
    }

    public static Indices de(String html) {
        Document doc = Jsoup.parse(html);
        return new Indices(indices(doc), dadosInstitucionais(doc), percentual(html));
    }

    private static Map<String, Indice> indices(Document doc) {
        Map<String, Indice> out = new LinkedHashMap<>();
        for (Element ac : doc.select("acronym[title]")) {
            String sigla = ac.text().replace(":", "").trim().toUpperCase();
            if (sigla.isEmpty()) continue;
            Element td = ac.closest("td");
            Element proximo = td == null ? null : td.nextElementSibling();
            String valor = proximo == null ? "" : proximo.text().trim();
            if (!valor.isEmpty()) {
                out.putIfAbsent(sigla, new Indice(valor, ac.attr("title").trim()));
            }
        }
        return out;
    }

    private static Map<String, String> dadosInstitucionais(Document doc) {
        Map<String, String> out = new LinkedHashMap<>();
        // rotulo do tipo "Status:" no ownText da celula (os indices ficam dentro de <acronym>,
        // entao o ownText do td deles e vazio e nao entra aqui).
        for (Element rotuloEl : doc.select("th, td")) {
            String texto = rotuloEl.ownText().trim();
            if (!texto.endsWith(":")) {
                continue;
            }
            Element valorEl = rotuloEl.nextElementSibling();
            if (valorEl == null) {
                continue;
            }
            String valor = valorEl.text().trim();
            if (!valor.isBlank() && !valor.endsWith(":")) {
                out.putIfAbsent(texto.substring(0, texto.length() - 1).trim(), valor);
            }
        }
        return out;
    }

    private static Integer percentual(String html) {
        Matcher m = PCT.matcher(html);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }
}
