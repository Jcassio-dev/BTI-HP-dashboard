package br.com.btihelpbot.bti_api.matricula;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Baixa e faz streaming de um CSV do dados.ufrn.br: separador ';', encoding latin1,
 * User-Agent de navegador (senao 403). Nunca carrega o arquivo inteiro em memoria.
 */
@Component
public class CsvDownloader {

    private final HttpClient http = HttpClient.newHttpClient();

    public interface RowHandler {
        void handle(Row row);
    }

    /** Acesso a uma linha por nome de coluna (robusto a reordenacao de colunas). */
    public static final class Row {
        private final Map<String, Integer> index;
        private final String[] values;

        Row(Map<String, Integer> index, String[] values) {
            this.index = index;
            this.values = values;
        }

        public String get(String column) {
            Integer i = index.get(column);
            if (i == null || i >= values.length) return null;
            return values[i];
        }
    }

    public void stream(String url, RowHandler handler) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", CkanClient.USER_AGENT)
                    .GET()
                    .build();
            HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStreamReader reader = new InputStreamReader(resp.body(), StandardCharsets.ISO_8859_1);
                 CSVReader csv = new CSVReaderBuilder(reader)
                         .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                         .build()) {
                String[] header = csv.readNext();
                if (header == null) return;
                Map<String, Integer> index = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    index.put(header[i].trim().toLowerCase(), i);
                }
                String[] row;
                while ((row = csv.readNext()) != null) {
                    handler.handle(new Row(index, row));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao baixar/parsear CSV: " + url, e);
        }
    }
}
