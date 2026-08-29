package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieJarTest {

    // Formato real do curl: cookie HttpOnly comeca com #HttpOnly_, e ha um JSESSIONID por dominio.
    private static final List<String> JAR = List.of(
            "# Netscape HTTP Cookie File",
            "# https://curl.se/docs/http-cookies.html",
            "",
            "#HttpOnly_autenticacao.ufrn.br\tFALSE\t/sso-server\tTRUE\t0\tJSESSIONID\tDO_CAS_123",
            "#HttpOnly_sigaa.ufrn.br\tFALSE\t/sigaa\tTRUE\t0\tJSESSIONID\tDO_SIGAA_456",
            "autenticacao.ufrn.br\tFALSE\t/\tTRUE\t0\tCASTGC\tTGC_789");

    @Test
    void pegaOJsessionidDoDominioDoSigaaMesmoSendoHttpOnly() {
        Optional<String> c = CurlNavegador.cookieDe(JAR, "JSESSIONID", "sigaa.ufrn.br");

        assertEquals(Optional.of("DO_SIGAA_456"), c);
    }

    @Test
    void naoConfundeComOJsessionidDoCas() {
        Optional<String> c = CurlNavegador.cookieDe(JAR, "JSESSIONID", "sigaa.ufrn.br");

        assertTrue(c.isPresent());
        assertEquals("DO_SIGAA_456", c.get());
    }

    @Test
    void leCookieNaoHttpOnlyNormalmente() {
        Optional<String> c = CurlNavegador.cookieDe(JAR, "CASTGC", "autenticacao.ufrn.br");

        assertEquals(Optional.of("TGC_789"), c);
    }

    @Test
    void semOCookieVoltaVazio() {
        assertTrue(CurlNavegador.cookieDe(JAR, "NAO_EXISTE", "sigaa.ufrn.br").isEmpty());
    }

    @Test
    void ignoraOsComentariosDeCabecalho() {
        assertTrue(CurlNavegador.cookieDe(List.of("# comentario", ""), "JSESSIONID", "sigaa.ufrn.br").isEmpty());
    }
}
