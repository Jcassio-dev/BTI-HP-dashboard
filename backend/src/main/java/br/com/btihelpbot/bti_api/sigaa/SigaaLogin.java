package br.com.btihelpbot.bti_api.sigaa;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Faz o login CAS da UFRN em nome do aluno e devolve o cookie de sessao do SIGAA.
 * A senha so vive durante esta chamada; nada e guardado aqui.
 *
 * Fluxo (o mesmo validado em explorar-sigaa.py):
 *   1. GET na tela do CAS, le action + campos escondidos (lt, execution, _eventId, submit)
 *   2. POST com esses campos + username/password
 *   3. sucesso -> CAS emite ticket, SIGAA abre sessao (JSESSIONID no jar)
 *      senha errada -> CAS remostra o formulario (tem campo password)
 *      conta bloqueada -> pagina "ACESSO BLOQUEADO"
 */
public class SigaaLogin {

    public static class CredenciaisInvalidas extends RuntimeException {
        public CredenciaisInvalidas() {
            super("Login ou senha do SIGAA incorretos.");
        }
    }

    public static class AcessoBloqueado extends RuntimeException {
        public AcessoBloqueado() {
            super("O SIGAA recusou o acesso desta conta.");
        }
    }

    private static final String ENTRADA =
            "https://autenticacao.ufrn.br/sso-server/login?service=https%3A%2F%2Fsigaa.ufrn.br%2Fsigaa%2Flogin%2Fcas";

    private final Navegador nav;

    public SigaaLogin(Navegador nav) {
        this.nav = nav;
    }

    public String logar(String usuario, String senha) {
        String tela = nav.get(ENTRADA);
        Document doc = Jsoup.parse(tela);

        Element form = doc.selectFirst("form");
        if (form == null) {
            throw new CurlImpersonateHttp.SigaaIndisponivel("A tela de login do SIGAA nao veio como esperado.");
        }
        String action = form.absUrl("action");
        if (action.isBlank()) {
            action = "https://autenticacao.ufrn.br" + form.attr("action");
        }

        Map<String, String> campos = new LinkedHashMap<>();
        for (Element in : form.select("input, button")) {
            String tipo = in.attr("type").toLowerCase();
            if (tipo.equals("password")) {
                continue;
            }
            String nome = in.attr("name");
            if (!nome.isBlank()) {
                campos.put(nome, in.attr("value"));
            }
        }
        campos.put("username", usuario);
        campos.put("password", senha);

        String resposta = nav.postForm(action, campos);

        if (resposta.toUpperCase().contains("ACESSO BLOQUEADO")) {
            throw new AcessoBloqueado();
        }
        if (Jsoup.parse(resposta).selectFirst("input[type=password]") != null) {
            throw new CredenciaisInvalidas();
        }

        return nav.cookie("JSESSIONID")
                .map(v -> "JSESSIONID=" + v)
                .orElseThrow(() -> new CurlImpersonateHttp.SigaaIndisponivel("O login passou mas nao veio a sessao."));
    }
}
