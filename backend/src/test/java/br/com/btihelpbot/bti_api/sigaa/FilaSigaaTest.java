package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilaSigaaTest {

    @Test
    void devolveOResultadoDaTarefa() throws Exception {
        FilaSigaa fila = new FilaSigaa(2, 10);

        assertEquals("pronto", fila.executar("aluno1", () -> "pronto"));
    }

    @Test
    void nuncaPassaDoLimiteDeRequisicoesSimultaneas() throws Exception {
        int limite = 2;
        int tarefas = 20;
        FilaSigaa fila = new FilaSigaa(limite, 100);

        AtomicInteger emVoo = new AtomicInteger();
        AtomicInteger pico = new AtomicInteger();
        CountDownLatch fim = new CountDownLatch(tarefas);
        ExecutorService pool = Executors.newFixedThreadPool(tarefas);

        for (int i = 0; i < tarefas; i++) {
            String aluno = "aluno" + i;
            pool.submit(() -> {
                try {
                    fila.executar(aluno, () -> {
                        int agora = emVoo.incrementAndGet();
                        pico.accumulateAndGet(agora, Math::max);
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        emVoo.decrementAndGet();
                        return null;
                    });
                } catch (Exception e) {
                    // contabilizado pelo latch
                } finally {
                    fim.countDown();
                }
            });
        }

        assertTrue(fim.await(30, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertTrue(pico.get() <= limite, "pico de " + pico.get() + " passou do limite de " + limite);
        assertTrue(pico.get() > 1, "o teste nao chegou a exercitar concorrencia");
    }

    @Test
    void oMesmoAlunoNaoEnfileiraDoisPedidosAoMesmoTempo() throws Exception {
        FilaSigaa fila = new FilaSigaa(4, 100);
        CountDownLatch segurando = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();

        pool.submit(() -> fila.executar("aluno1", () -> {
            segurando.countDown();
            try {
                liberar.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }));

        assertTrue(segurando.await(5, TimeUnit.SECONDS));
        assertThrows(FilaSigaa.JaEmVoo.class, () -> fila.executar("aluno1", () -> null));

        liberar.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void alunoLiberaAVagaDepoisDeTerminar() throws Exception {
        FilaSigaa fila = new FilaSigaa(2, 10);

        fila.executar("aluno1", () -> "primeira");

        assertEquals("segunda", fila.executar("aluno1", () -> "segunda"));
    }

    @Test
    void alunoLiberaAVagaMesmoQuandoATarefaFalha() {
        FilaSigaa fila = new FilaSigaa(2, 10);

        assertThrows(RuntimeException.class,
                () -> fila.executar("aluno1", () -> { throw new RuntimeException("caiu"); }));

        assertEquals("ok", fila.executar("aluno1", () -> "ok"));
    }

    @Test
    void recusaQuandoAFilaEnche() throws Exception {
        FilaSigaa fila = new FilaSigaa(1, 1);
        CountDownLatch segurando = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        pool.submit(() -> fila.executar("ocupado", () -> {
            segurando.countDown();
            try {
                liberar.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }));
        assertTrue(segurando.await(5, TimeUnit.SECONDS));

        // um espera na fila (profundidade 1)
        pool.submit(() -> fila.executar("esperando", () -> null));
        Thread.sleep(100);

        // o proximo nao cabe
        List<Exception> erros = new ArrayList<>();
        try {
            fila.executar("sobrando", () -> null);
        } catch (Exception e) {
            erros.add(e);
        }

        liberar.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, erros.size());
        assertTrue(erros.get(0) instanceof FilaSigaa.FilaCheia);
    }
}
