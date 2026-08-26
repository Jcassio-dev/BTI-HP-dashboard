package br.com.btihelpbot.bti_api.sigaa;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token de uso unico que liga o numero de WhatsApp a uma sessao aberta no navegador.
 * Vive em memoria de proposito: dura minutos, e perder tudo num restart e aceitavel.
 */
public class VinculoService {

    private record Pendente(String jid, Instant vence) {}

    private final Map<String, Pendente> porToken = new ConcurrentHashMap<>();
    private final Map<String, String> tokenDoJid = new ConcurrentHashMap<>();
    private final SecureRandom aleatorio = new SecureRandom();
    private final Clock relogio;
    private final Duration validade;

    public VinculoService(Clock relogio, Duration validade) {
        this.relogio = relogio;
        this.validade = validade;
    }

    public String gerar(String jid) {
        String anterior = tokenDoJid.remove(jid);
        if (anterior != null) {
            porToken.remove(anterior);
        }

        byte[] bytes = new byte[24];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        porToken.put(token, new Pendente(jid, relogio.instant().plus(validade)));
        tokenDoJid.put(jid, token);
        return token;
    }

    public Optional<String> consumir(String token) {
        Pendente p = porToken.remove(token);
        if (p == null) {
            return Optional.empty();
        }
        tokenDoJid.remove(p.jid(), token);
        if (!p.vence().isAfter(relogio.instant())) {
            return Optional.empty();
        }
        return Optional.of(p.jid());
    }
}
