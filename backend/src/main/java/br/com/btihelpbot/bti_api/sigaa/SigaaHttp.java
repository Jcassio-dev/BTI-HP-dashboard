package br.com.btihelpbot.bti_api.sigaa;

/** Busca uma pagina do SIGAA usando um cookie de sessao. A impl real usa curl-impersonate. */
@FunctionalInterface
public interface SigaaHttp {
    String get(String url, String cookie);
}
