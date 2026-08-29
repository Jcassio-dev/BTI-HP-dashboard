package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CurlHttpTest {

    @Test
    void montaOJarComONomeEValorSeparados() {
        String jar = CurlImpersonateHttp.jarNetscape("JSESSIONID=abc123");

        assertTrue(jar.contains("sigaa.ufrn.br"), "precisa mirar o dominio do SIGAA");
        assertTrue(jar.contains("\tJSESSIONID\tabc123\n"), "nome e valor em colunas separadas");
    }

    @Test
    void semSinalDeIgualUsaJsessionidComoNome() {
        String jar = CurlImpersonateHttp.jarNetscape("sovalor");

        assertTrue(jar.contains("\tJSESSIONID\tsovalor\n"));
    }
}
