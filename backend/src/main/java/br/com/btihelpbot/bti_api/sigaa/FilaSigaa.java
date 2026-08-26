package br.com.btihelpbot.bti_api.sigaa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Contem quantas requisicoes chegam ao SIGAA ao mesmo tempo. O limite existe por educacao com
 * o servidor da universidade, nao por capacidade desta maquina.
 */
public class FilaSigaa {

    /** O aluno ja tem um pedido em andamento. */
    public static class JaEmVoo extends RuntimeException {
        public JaEmVoo() {
            super("Já tem um pedido seu em andamento. Espera ele terminar.");
        }
    }

    /** A fila encheu; recusar e melhor que deixar o aluno esperando ate dar timeout. */
    public static class FilaCheia extends RuntimeException {
        public FilaCheia() {
            super("O SIGAA está congestionado agora. Tenta de novo em um minuto.");
        }
    }

    private final Semaphore vagas;
    private final int profundidadeMax;
    private final AtomicInteger esperando = new AtomicInteger();
    private final Map<String, Boolean> emVoo = new ConcurrentHashMap<>();

    public FilaSigaa(int concorrencia, int profundidadeMax) {
        this.vagas = new Semaphore(concorrencia, true);
        this.profundidadeMax = profundidadeMax;
    }

    public <T> T executar(String aluno, Supplier<T> tarefa) {
        if (emVoo.putIfAbsent(aluno, Boolean.TRUE) != null) {
            throw new JaEmVoo();
        }

        try {
            if (esperando.incrementAndGet() > profundidadeMax) {
                throw new FilaCheia();
            }
            try {
                vagas.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Pedido interrompido", e);
            }
            try {
                return tarefa.get();
            } finally {
                vagas.release();
            }
        } finally {
            esperando.decrementAndGet();
            emVoo.remove(aluno);
        }
    }

    public int esperandoAgora() {
        return Math.max(esperando.get() - vagas.availablePermits(), 0);
    }
}
