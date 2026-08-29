package br.com.btihelpbot.bti_api.sigaa;

/** O aluno precisa (re)conectar: sem foto guardada, ou a sessao caiu no meio da coleta. */
public class PrecisaConectar extends RuntimeException {
    public PrecisaConectar() {
        super("Conecte sua conta do SIGAA com !conectar.");
    }
}
