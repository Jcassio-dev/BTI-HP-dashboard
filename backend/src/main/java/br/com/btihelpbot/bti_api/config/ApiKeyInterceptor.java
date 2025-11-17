package br.com.btihelpbot.bti_api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${api.security.key}")
    private String secretApiKey;

    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || !requestApiKey.equals(secretApiKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chave de API inválida ou ausente");
            return false;
        }

        return true;
    }
}