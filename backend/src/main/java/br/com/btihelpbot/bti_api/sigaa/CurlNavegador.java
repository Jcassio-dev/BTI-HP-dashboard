package br.com.btihelpbot.bti_api.sigaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Navegador com estado sobre curl-impersonate: um cookie jar em arquivo temporario guarda a
 * sessao entre as chamadas do fluxo CAS, e segue redirecionamento. O corpo do POST (com a senha)
 * vai por arquivo, nunca na linha de comando, para nao vazar no ps. Descartavel: feche apos usar.
 */
public class CurlNavegador implements Navegador, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(CurlNavegador.class);
    private static final long TIMEOUT = 30;

    private final String binario;
    private final Path jar;

    public CurlNavegador(String binario) {
        this.binario = binario == null || binario.isBlank() ? "curl_chrome131" : binario;
        try {
            this.jar = Files.createTempFile("sigaa-jar-", ".txt");
        } catch (IOException e) {
            throw new CurlImpersonateHttp.SigaaIndisponivel("Nao consegui preparar o cliente de login.");
        }
    }

    @Override
    public String get(String url) {
        return executar(List.of("--location", url));
    }

    @Override
    public String postForm(String url, Map<String, String> campos) {
        StringBuilder corpo = new StringBuilder();
        for (Map.Entry<String, String> e : campos.entrySet()) {
            if (corpo.length() > 0) corpo.append('&');
            corpo.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        Path body = null;
        try {
            body = Files.createTempFile("sigaa-post-", ".txt");
            Files.writeString(body, corpo.toString());
            return executar(List.of("--location", "--data", "@" + body, url));
        } catch (IOException e) {
            throw new CurlImpersonateHttp.SigaaIndisponivel("Nao consegui montar o login.");
        } finally {
            apagar(body);
        }
    }

    private static final String DOMINIO_SIGAA = "sigaa.ufrn.br";

    @Override
    public Optional<String> cookie(String nome) {
        try {
            return cookieDe(Files.readAllLines(jar), nome, DOMINIO_SIGAA);
        } catch (IOException e) {
            log.warn("nao consegui ler o cookie jar", e);
            return Optional.empty();
        }
    }

    /**
     * Le o cookie jar Netscape do curl. Cookie HttpOnly comeca com "#HttpOnly_" e ainda e cookie,
     * nao comentario. Como ha um JSESSIONID por dominio (CAS e SIGAA), prefere o do dominio pedido.
     */
    static Optional<String> cookieDe(java.util.List<String> linhas, String nome, String dominio) {
        Optional<String> fallback = Optional.empty();
        for (String linha : linhas) {
            String l = linha.startsWith("#HttpOnly_") ? linha.substring("#HttpOnly_".length()) : linha;
            if (l.startsWith("#") || l.isBlank()) {
                continue;
            }
            String[] col = l.split("\t");
            if (col.length >= 7 && col[5].equals(nome)) {
                if (col[0].contains(dominio)) {
                    return Optional.of(col[6]);
                }
                fallback = Optional.of(col[6]);
            }
        }
        return fallback;
    }

    private String executar(List<String> extra) {
        List<String> cmd = new ArrayList<>(List.of(
                binario, "--silent", "--show-error",
                "--max-time", String.valueOf(TIMEOUT),
                "--cookie", jar.toString(),
                "--cookie-jar", jar.toString()));
        cmd.addAll(extra);

        try {
            Process p = new ProcessBuilder(cmd).start();
            String corpo = ler(p.getInputStream());
            if (!p.waitFor(TIMEOUT + 5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new CurlImpersonateHttp.SigaaIndisponivel("O SIGAA demorou demais para responder.");
            }
            if (p.exitValue() != 0) {
                throw new CurlImpersonateHttp.SigaaIndisponivel("Nao consegui falar com o SIGAA agora.");
            }
            return corpo;
        } catch (IOException e) {
            throw new CurlImpersonateHttp.SigaaIndisponivel("O cliente de login do SIGAA nao esta disponivel.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CurlImpersonateHttp.SigaaIndisponivel("Login ao SIGAA interrompido.");
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String ler(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private void apagar(Path p) {
        if (p == null) return;
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            log.warn("nao consegui apagar arquivo temporario de login", e);
        }
    }

    @Override
    public void close() {
        apagar(jar);
    }
}
