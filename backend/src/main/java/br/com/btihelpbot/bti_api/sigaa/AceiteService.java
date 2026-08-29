package br.com.btihelpbot.bti_api.sigaa;

import java.time.Clock;

/** Grava o aceite dos termos como registro de auditoria, separado da foto dos dados. */
public class AceiteService {

    private final AceiteTermosRepository repository;
    private final Clock relogio;

    public AceiteService(AceiteTermosRepository repository, Clock relogio) {
        this.repository = repository;
        this.relogio = relogio;
    }

    public void registrar(String jid, String versao, String ip) {
        AceiteTermos a = new AceiteTermos();
        a.setJid(jid);
        a.setVersao(versao);
        a.setIp(ip);
        a.setAceitoEm(relogio.instant());
        repository.save(a);
    }
}
