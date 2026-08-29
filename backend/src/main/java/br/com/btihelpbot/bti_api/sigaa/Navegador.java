package br.com.btihelpbot.bti_api.sigaa;

import java.util.Map;
import java.util.Optional;

/**
 * Cliente HTTP com estado (segue redirecionamento, guarda cookies) para o fluxo de login CAS.
 * A impl real usa curl-impersonate com um cookie jar temporario.
 */
public interface Navegador {
    String get(String url);

    String postForm(String url, Map<String, String> campos);

    Optional<String> cookie(String nome);
}
