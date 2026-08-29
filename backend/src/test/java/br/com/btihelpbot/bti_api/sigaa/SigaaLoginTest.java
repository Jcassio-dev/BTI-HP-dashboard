package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SigaaLoginTest {

    private static final String CAS = """
        <html><body>
          <form id="login-form" action="/sso-server/login;jsessionid=ABC?service=x" method="post">
            <input name="username" type="text"/>
            <input name="password" type="password"/>
            <input name="lt" type="hidden" value="LT-123"/>
            <input name="execution" type="hidden" value="e1s1"/>
            <input name="_eventId" type="hidden" value="submit"/>
            <button name="submit" value="Submit">Entrar</button>
          </form>
        </body></html>
        """;

    private static final String PORTAL = "<html><body><h1>Portal do Discente</h1></body></html>";
    private static final String BLOQUEADO = "<html><body>ACESSO BLOQUEADO</body></html>";
    private static final String CAS_DE_NOVO = CAS; // credencial errada: CAS remostra o login

    /** Navegador falso com jar em memoria, roteando por URL/estado. */
    private static class NavegadorFake implements Navegador {
        final Map<String, String> cookies = new LinkedHashMap<>();
        Map<String, String> ultimoPost;
        String respostaLogin = PORTAL;

        public String get(String url) {
            return CAS;
        }

        public String postForm(String url, Map<String, String> campos) {
            this.ultimoPost = campos;
            if (respostaLogin.equals(PORTAL)) {
                cookies.put("JSESSIONID", "sessao-nova");
            }
            return respostaLogin;
        }

        public Optional<String> cookie(String nome) {
            return Optional.ofNullable(cookies.get(nome));
        }
    }

    @Test
    void mandaOsCamposDoFormularioMaisUsuarioESenha() {
        NavegadorFake nav = new NavegadorFake();

        new SigaaLogin(nav).logar("cassio", "segredo");

        assertEquals("cassio", nav.ultimoPost.get("username"));
        assertEquals("segredo", nav.ultimoPost.get("password"));
        assertEquals("LT-123", nav.ultimoPost.get("lt"));
        assertEquals("e1s1", nav.ultimoPost.get("execution"));
        assertEquals("Submit", nav.ultimoPost.get("submit"));
    }

    @Test
    void devolveOCookieDaSessaoQuandoOLoginPassa() {
        NavegadorFake nav = new NavegadorFake();

        String cookie = new SigaaLogin(nav).logar("cassio", "segredo");

        assertEquals("JSESSIONID=sessao-nova", cookie);
    }

    @Test
    void senhaErradaLancaCredenciaisInvalidas() {
        NavegadorFake nav = new NavegadorFake();
        nav.respostaLogin = CAS_DE_NOVO; // CAS remostrou o formulario de login

        assertThrows(SigaaLogin.CredenciaisInvalidas.class,
                () -> new SigaaLogin(nav).logar("cassio", "errada"));
    }

    @Test
    void contaBloqueadaNoSigaaLancaBloqueio() {
        NavegadorFake nav = new NavegadorFake();
        nav.respostaLogin = BLOQUEADO;

        assertThrows(SigaaLogin.AcessoBloqueado.class,
                () -> new SigaaLogin(nav).logar("cassio", "segredo"));
    }

    @Test
    void aSenhaNuncaVaiParaUmCampoQueNaoSejaPassword() {
        NavegadorFake nav = new NavegadorFake();

        new SigaaLogin(nav).logar("cassio", "segredo");

        long ondeApareceASenha = nav.ultimoPost.entrySet().stream()
                .filter(e -> e.getValue().equals("segredo"))
                .count();
        assertEquals(1, ondeApareceASenha);
        assertTrue(nav.ultimoPost.containsKey("password"));
    }
}
