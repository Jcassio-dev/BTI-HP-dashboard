package br.com.btihelpbot.bti_api.sigaa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Busca paginas do SIGAA com curl-impersonate, que imita a assinatura TLS do Chrome e por isso
 * passa pelo filtro anti-robo da UFRN. O caminho do binario vem de SIGAA_CURL (default:
 * curl_chrome131). O cookie de sessao vai por um cookie jar em arquivo, nunca na linha de comando.
 */
public class CurlImpersonateHttp implements SigaaHttp {

    private static final Logger log = LoggerFactory.getLogger(CurlImpersonateHttp.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String binario;

    public CurlImpersonateHttp(String binario) {
        this.binario = binario == null || binario.isBlank() ? "curl_chrome131" : binario;
    }

    @Override
    public String get(String url, String cookie) {
        Path jar = null;
        try {
            jar = Files.createTempFile("sigaa-cookie-", ".txt");
            Files.writeString(jar, jarNetscape(cookie));

            ProcessBuilder pb = new ProcessBuilder(
                    binario,
                    "--silent", "--show-error",
                    "--location",
                    "--max-time", String.valueOf(TIMEOUT.toSeconds()),
                    "--cookie", jar.toString(),
                    url);
            Process p = pb.start();
            String corpo = ler(p.getInputStream());
            if (!p.waitFor(TIMEOUT.toSeconds() + 5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new SigaaIndisponivel("O SIGAA demorou demais para responder.");
            }
            if (p.exitValue() != 0) {
                log.warn("curl-impersonate saiu com codigo {}", p.exitValue());
                throw new SigaaIndisponivel("Nao consegui falar com o SIGAA agora.");
            }
            return corpo;
        } catch (IOException e) {
            log.error("curl-impersonate ({}) nao pode ser executado", binario, e);
            throw new SigaaIndisponivel("O cliente de acesso ao SIGAA nao esta disponivel.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SigaaIndisponivel("Consulta ao SIGAA interrompida.");
        } finally {
            apagar(jar);
        }
    }

    /** Monta um cookie jar Netscape com o cookie de sessao para o dominio do SIGAA. */
    static String jarNetscape(String cookie) {
        int i = cookie.indexOf('=');
        String nome = i > 0 ? cookie.substring(0, i) : "JSESSIONID";
        String valor = i > 0 ? cookie.substring(i + 1) : cookie;
        return "# Netscape HTTP Cookie File\n"
                + String.join("\t", "#HttpOnly_sigaa.ufrn.br", "FALSE", "/", "FALSE", "0", nome, valor)
                + "\n";
    }

    private void apagar(Path p) {
        if (p == null) return;
        try {
            Files.deleteIfExists(p);
        } catch (IOException e) {
            log.warn("nao consegui apagar o cookie temporario", e);
        }
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

    /** O SIGAA (ou o cliente) nao respondeu; nao e caso de reconectar, e so tentar de novo. */
    public static class SigaaIndisponivel extends RuntimeException {
        public SigaaIndisponivel(String msg) {
            super(msg);
        }
    }
}
