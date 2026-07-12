package br.com.btihelpbot.bti_api.matricula;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/** Resolve os recursos (CSVs) de um dataset do portal de dados abertos da UFRN. */
@Component
public class CkanClient {

    private static final String BASE = "https://dados.ufrn.br";
    static final String USER_AGENT = "Mozilla/5.0 (compatible; BTIHelpBot/1.0)";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public record Resource(String name, String url, String format) {}

    public List<Resource> getResources(String packageId) {
        try {
            HttpRequest req = HttpRequest.newBuilder(
                            URI.create(BASE + "/api/3/action/package_show?id=" + packageId))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode resources = mapper.readTree(resp.body()).path("result").path("resources");
            List<Resource> out = new ArrayList<>();
            for (JsonNode r : resources) {
                out.add(new Resource(r.path("name").asText(), r.path("url").asText(), r.path("format").asText()));
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao consultar CKAN: " + packageId, e);
        }
    }

    /** So os recursos que sao CSV de fato (fora dicionarios em PDF). */
    public static List<Resource> onlyCsv(List<Resource> resources) {
        List<Resource> out = new ArrayList<>();
        for (Resource r : resources) {
            boolean isCsv = "CSV".equalsIgnoreCase(r.format()) || (r.url() != null && r.url().toLowerCase().endsWith(".csv"));
            if (isCsv) out.add(r);
        }
        return out;
    }
}
