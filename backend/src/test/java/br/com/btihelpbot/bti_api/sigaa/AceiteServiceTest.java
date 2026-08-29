package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AceiteServiceTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-08-29T18:00:00Z"), ZoneOffset.UTC);

    @Test
    void gravaOAceiteComVersaoIpEData() {
        AceiteTermosRepository repo = mock(AceiteTermosRepository.class);
        AceiteService servico = new AceiteService(repo, relogio);

        servico.registrar("aluno@jid", "2026-08-29", "200.1.2.3");

        ArgumentCaptor<AceiteTermos> captor = ArgumentCaptor.forClass(AceiteTermos.class);
        verify(repo).save(captor.capture());
        AceiteTermos gravado = captor.getValue();

        assertEquals("aluno@jid", gravado.getJid());
        assertEquals("2026-08-29", gravado.getVersao());
        assertEquals("200.1.2.3", gravado.getIp());
        assertEquals(Instant.parse("2026-08-29T18:00:00Z"), gravado.getAceitoEm());
    }
}
