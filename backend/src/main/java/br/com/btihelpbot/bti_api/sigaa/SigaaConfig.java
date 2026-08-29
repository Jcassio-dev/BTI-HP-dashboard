package br.com.btihelpbot.bti_api.sigaa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class SigaaConfig {

    @Bean
    Clock relogioSigaa() {
        return Clock.systemUTC();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SigaaConfig.class);

    @Bean
    CofreSessao cofreSessao(@Value("${sigaa.chave-cofre:}") String chave) {
        if (chave == null || chave.isBlank()) {
            log.warn("SIGAA_CHAVE_COFRE nao definida: usando chave efemera. "
                    + "As sessoes do SIGAA nao sobrevivem a um restart. Defina a variavel em producao.");
            return new CofreSessao(CofreSessao.gerarChave());
        }
        return new CofreSessao(chave);
    }

    @Bean
    SnapshotService snapshotService(SnapshotSigaaRepository repo, CofreSessao cofre,
                                    com.fasterxml.jackson.databind.ObjectMapper mapper) {
        return new SnapshotService(repo, cofre, mapper);
    }

    @Bean
    FilaSigaa filaSigaa(@Value("${sigaa.concorrencia:2}") int concorrencia,
                        @Value("${sigaa.fila-max:50}") int filaMax) {
        return new FilaSigaa(concorrencia, filaMax);
    }

    @Bean
    SigaaHttp sigaaHttp(@Value("${sigaa.curl:curl_chrome131}") String binario) {
        return new CurlImpersonateHttp(binario);
    }

    @Bean
    Coletor coletor(FilaSigaa fila, Clock relogioSigaa) {
        return new Coletor(fila, relogioSigaa);
    }

    @Bean
    VinculoService vinculoService(Clock relogioSigaa,
                                  @Value("${sigaa.validade-token-min:10}") long minutos) {
        return new VinculoService(relogioSigaa, Duration.ofMinutes(minutos));
    }
}
