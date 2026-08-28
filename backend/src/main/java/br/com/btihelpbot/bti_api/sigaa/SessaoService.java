package br.com.btihelpbot.bti_api.sigaa;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

/**
 * Guarda a sessao do SIGAA de cada aluno, cifrada, com prazo. Nunca guarda senha.
 * Quando a sessao vence, o aluno reconecta pela pagina de login.
 */
public class SessaoService {

    private final SessaoSigaaRepository repository;
    private final CofreSessao cofre;
    private final Clock relogio;
    private final Duration validade;

    public SessaoService(SessaoSigaaRepository repository, CofreSessao cofre,
                         Clock relogio, Duration validade) {
        this.repository = repository;
        this.cofre = cofre;
        this.relogio = relogio;
        this.validade = validade;
    }

    public void salvar(String jid, String cookie) {
        SessaoSigaa s = repository.findById(jid).orElseGet(SessaoSigaa::new);
        s.setJid(jid);
        s.setCookieCifrado(cofre.cifrar(cookie));
        s.setCriadaEm(relogio.instant());
        s.setVenceEm(relogio.instant().plus(validade));
        repository.save(s);
    }

    public Optional<String> cookieDe(String jid) {
        return repository.findById(jid)
                .filter(s -> s.getVenceEm().isAfter(relogio.instant()))
                .map(s -> cofre.decifrar(s.getCookieCifrado()));
    }

    public boolean temSessao(String jid) {
        return cookieDe(jid).isPresent();
    }

    public void esquecer(String jid) {
        repository.deleteById(jid);
    }
}
