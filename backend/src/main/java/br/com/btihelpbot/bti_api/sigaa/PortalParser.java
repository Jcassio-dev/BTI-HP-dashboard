package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Le a pagina do portal do discente do SIGAA. So estrutura; nao guarda nada. */
public final class PortalParser {

    public record Turma(String codigo, String nome, String local, String horario) {}

    private static final Pattern COD_NOME = Pattern.compile("^\\s*([A-Z]{2,}\\d{3,})\\s*-\\s*(.+)$");
    private static final Pattern HORARIO = Pattern.compile("^(\\d+)([MTN])(\\d+)$");

    private static final String[] DIAS = {"", "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
    private static final java.util.Map<Character, String> TURNO = java.util.Map.of(
            'M', "manhã", 'T', "tarde", 'N', "noite");

    private PortalParser() {}

    public static boolean autenticado(String html) {
        Document doc = Jsoup.parse(html);
        boolean temLogin = doc.selectFirst("input[type=password]") != null;
        boolean bloqueado = html.toUpperCase().contains("ACESSO BLOQUEADO");
        return !temLogin && !bloqueado;
    }

    public static List<Turma> turmas(String html) {
        List<Turma> out = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Element tabela = tabelaDeTurmas(doc);
        if (tabela == null) {
            return out;
        }

        for (Element tr : tabela.select("tbody > tr")) {
            Element descricao = tr.selectFirst("td.descricao");
            if (descricao == null) {
                continue; // linha de agrupamento (numero do dia, etc.)
            }
            Element link = descricao.selectFirst("a");
            String rotulo = link != null ? link.text().trim() : descricao.text().trim();
            if (rotulo.isEmpty()) {
                continue;
            }

            List<Element> infos = tr.select("td.info");
            String local = infos.size() > 0 ? infos.get(0).text().trim() : "";
            String horario = infos.size() > 1 ? infos.get(1).text().trim() : "";

            out.add(montar(rotulo, local, horario));
        }
        return out;
    }

    private static Element tabelaDeTurmas(Document doc) {
        for (Element t : doc.select("table")) {
            String texto = t.text();
            if (texto.contains("Componente Curricular") && texto.contains("Horário")) {
                return t;
            }
        }
        return null;
    }

    private static Turma montar(String rotulo, String local, String horario) {
        Matcher m = COD_NOME.matcher(rotulo);
        if (m.matches()) {
            return new Turma(m.group(1), tituloCase(m.group(2)), local, horario);
        }
        return new Turma("", tituloCase(rotulo), local, horario);
    }

    public static String horarioLegivel(String codigo) {
        Matcher m = HORARIO.matcher(codigo);
        if (!m.matches()) {
            return codigo;
        }
        String dias = diasLegiveis(m.group(1));
        String turno = TURNO.getOrDefault(m.group(2).charAt(0), m.group(2));
        String h = m.group(3);
        String faixa = "%sº-%sº".formatted(h.charAt(0), h.charAt(h.length() - 1));
        return "%s, %s (%s)".formatted(dias, turno, faixa);
    }

    private static String diasLegiveis(String digitos) {
        List<String> partes = new ArrayList<>();
        for (char c : digitos.toCharArray()) {
            int i = c - '0';
            if (i >= 1 && i < DIAS.length) {
                partes.add(DIAS[i]);
            }
        }
        return String.join(" e ", partes);
    }

    private static final java.util.Set<String> MENORES = java.util.Set.of(
            "de", "da", "do", "das", "dos", "e", "em", "a", "o", "as", "os", "para", "com", "por");

    private static String tituloCase(String texto) {
        String bruto = texto.trim();
        if (!bruto.equals(bruto.toUpperCase())) {
            return bruto;
        }
        String[] palavras = bruto.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palavras.length; i++) {
            String p = palavras[i];
            if (i > 0) sb.append(' ');
            if (i > 0 && MENORES.contains(p)) {
                sb.append(p);
            } else if (p.matches("[ivx]+")) {
                sb.append(p.toUpperCase());
            } else if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
