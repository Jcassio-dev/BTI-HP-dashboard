package br.com.btihelpbot.bti_api.sigaa;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Janela deslizante por chave (IP). Segura tentativas de login para nao martelar o CAS da UFRN
 * caso um token vaze. Em memoria: uma instancia so, chega para o volume esperado.
 */
public class LimitadorLogin {

    private final Clock relogio;
    private final int max;
    private final Duration janela;
    private final Map<String, Deque<Instant>> tentativas = new ConcurrentHashMap<>();

    public LimitadorLogin(Clock relogio, int max, Duration janela) {
        this.relogio = relogio;
        this.max = max;
        this.janela = janela;
    }

    public boolean permitir(String chave) {
        Instant agora = relogio.instant();
        Instant corte = agora.minus(janela);
        Deque<Instant> fila = tentativas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (fila) {
            while (!fila.isEmpty() && fila.peekFirst().isBefore(corte)) {
                fila.pollFirst();
            }
            if (fila.size() >= max) {
                return false;
            }
            fila.addLast(agora);
            return true;
        }
    }
}
