package br.com.btihelpbot.bti_api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${ratelimit.turmas.capacidade:30}")
    private int capacidade;

    @Value("${ratelimit.turmas.janela-segundos:60}")
    private long janelaSegundos;

    private final ConcurrentHashMap<String, Contador> contadores = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Contador contador = contadores.computeIfAbsent(ipDe(request), k -> new Contador());

        long agora = System.currentTimeMillis() / 1000;
        int restantes;
        long resetEm;

        synchronized (contador) {
            if (agora - contador.inicioJanela >= janelaSegundos) {
                contador.inicioJanela = agora;
                contador.requisicoes = 0;
            }
            contador.requisicoes++;
            restantes = capacidade - contador.requisicoes;
            resetEm = contador.inicioJanela + janelaSegundos;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacidade));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(restantes, 0)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetEm));

        if (restantes < 0) {
            response.setHeader("Retry-After", String.valueOf(resetEm - agora));
            response.setStatus(429);
            return false;
        }
        return true;
    }

    private static String ipDe(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Contador {
        long inicioJanela = System.currentTimeMillis() / 1000;
        int requisicoes = 0;
    }
}
