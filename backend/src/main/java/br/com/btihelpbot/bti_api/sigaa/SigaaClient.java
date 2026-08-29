package br.com.btihelpbot.bti_api.sigaa;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Consulta o SIGAA em nome de um aluno, usando a sessao guardada. Passa pela fila (limite de
 * requisicoes ao SIGAA) e pelo cache (evita bater de novo pelo mesmo dado). Nao faz login:
 * quando nao ha sessao valida, pede para o aluno reconectar.
 */
public class SigaaClient {

    /** O aluno precisa (re)conectar: sem sessao, ou a sessao expirou. */
    public static class PrecisaConectar extends RuntimeException {
        public PrecisaConectar() {
            super("Conecte sua conta do SIGAA para usar este comando.");
        }
    }

    private static final String PORTAL = "https://sigaa.ufrn.br/sigaa/portais/discente/discente.jsf";
    private static final Duration TTL_TURMAS = Duration.ofHours(12);

    private final SigaaHttp http;
    private final SessaoService sessoes;
    private final CacheSigaa cache;
    private final FilaSigaa fila;

    public SigaaClient(SigaaHttp http, SessaoService sessoes, CacheSigaa cache, FilaSigaa fila) {
        this.http = http;
        this.sessoes = sessoes;
        this.cache = cache;
        this.fila = fila;
    }

    @SuppressWarnings("unchecked")
    public List<PortalParser.Turma> turmas(String jid) {
        Optional<List<PortalParser.Turma>> emCache = cache.obter(jid, "turmas", (Class<List<PortalParser.Turma>>) (Class<?>) List.class);
        if (emCache.isPresent()) {
            return emCache.get();
        }

        String cookie = sessoes.cookieDe(jid).orElseThrow(PrecisaConectar::new);

        String html = fila.executar(jid, () -> http.get(PORTAL, cookie));
        if (!PortalParser.autenticado(html)) {
            // a sessao morreu no meio do caminho: limpa tudo e manda reconectar
            sessoes.esquecer(jid);
            cache.invalidar(jid);
            throw new PrecisaConectar();
        }

        List<PortalParser.Turma> turmas = PortalParser.turmas(html);
        cache.guardar(jid, "turmas", turmas, TTL_TURMAS);
        return turmas;
    }

    /** Descarta o cache do aluno; a proxima consulta busca de novo no SIGAA. */
    public void atualizar(String jid) {
        cache.invalidar(jid);
    }
}
