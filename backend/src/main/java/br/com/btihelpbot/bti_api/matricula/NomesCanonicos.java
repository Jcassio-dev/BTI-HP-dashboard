package br.com.btihelpbot.bti_api.matricula;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Os dados abertos trazem o mesmo nome com e sem acento. Escolhe, entre as grafias que
 * existem na fonte, a mais acentuada. Nunca cria acento que nao esteja nos dados.
 */
public final class NomesCanonicos {

    private final Map<String, String> porChave;

    private NomesCanonicos(Map<String, String> porChave) {
        this.porChave = porChave;
    }

    public static NomesCanonicos de(Collection<String> nomes) {
        Map<String, String> melhor = new HashMap<>();
        for (String nome : nomes) {
            if (nome == null || nome.isBlank()) continue;
            String chave = chave(nome);
            String atual = melhor.get(chave);
            if (atual == null || preferir(nome, atual)) {
                melhor.put(chave, nome);
            }
        }
        return new NomesCanonicos(melhor);
    }

    public String melhor(String nome) {
        if (nome == null || nome.isBlank()) return nome;
        return porChave.getOrDefault(chave(nome), nome);
    }

    private static boolean preferir(String candidato, String atual) {
        int a = acentos(candidato);
        int b = acentos(atual);
        if (a != b) return a > b;
        return candidato.compareTo(atual) < 0;
    }

    private static int acentos(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^\\p{M}]", "").length();
    }

    private static String chave(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
    }
}
