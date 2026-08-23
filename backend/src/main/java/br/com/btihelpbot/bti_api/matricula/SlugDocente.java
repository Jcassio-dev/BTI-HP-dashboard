package br.com.btihelpbot.bti_api.matricula;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SlugDocente {

    private SlugDocente() {}

    public static Map<String, String> porSiape(Map<String, Docente> docentes) {
        Map<String, Set<String>> siapesPorSlug = new HashMap<>();
        docentes.forEach((siape, d) -> {
            String base = slug(d.nome());
            if (!base.isEmpty()) {
                siapesPorSlug.computeIfAbsent(base, k -> new HashSet<>()).add(siape);
            }
        });

        Map<String, String> out = new LinkedHashMap<>();
        docentes.forEach((siape, d) -> {
            String base = slug(d.nome());
            if (base.isEmpty()) return;
            boolean colide = siapesPorSlug.get(base).size() > 1;
            String lotacao = slug(d.lotacao() == null ? "" : d.lotacao());
            out.put(siape, colide && !lotacao.isEmpty() ? base + "-" + lotacao : base);
        });
        return out;
    }

    public static String slug(String texto) {
        if (texto == null) return "";
        String sem = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return sem.replaceAll("[^a-z0-9]+", "-").replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    }
}
