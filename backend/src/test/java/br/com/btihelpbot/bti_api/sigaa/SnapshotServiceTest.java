package br.com.btihelpbot.bti_api.sigaa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotServiceTest {

    private Map<String, SnapshotSigaa> banco;
    private SnapshotService servico;

    private static DadosSigaa exemplo() {
        return new DadosSigaa(
                List.of(new PortalParser.Turma("IMD0030", "Algoritmos", "IMD - A104", "24M34")),
                List.of(new DadosSigaa.Indice("IRA", "6.7931", "Índice de Rendimento Acadêmico")),
                Map.of("Status", "ATIVO"),
                62,
                List.of(),
                Instant.parse("2026-08-29T15:00:00Z"));
    }

    @BeforeEach
    void preparar() {
        banco = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        servico = new SnapshotService(new SnapshotRepoFake(banco), new CofreSessao(CofreSessao.gerarChave()), mapper);
    }

    @Test
    void guardaEDevolveOsDadosIntactos() {
        servico.salvar("aluno@jid", exemplo());

        Optional<DadosSigaa> lido = servico.obter("aluno@jid");

        assertTrue(lido.isPresent());
        assertEquals("6.7931", lido.get().indices().get(0).valor());
        assertEquals("Algoritmos", lido.get().turmas().get(0).nome());
        assertEquals(62, lido.get().integralizado());
        assertEquals(Instant.parse("2026-08-29T15:00:00Z"), lido.get().atualizadoEm());
    }

    @Test
    void osDadosNaoFicamEmClaroNoBanco() {
        servico.salvar("aluno@jid", exemplo());

        assertFalse(banco.get("aluno@jid").getDadosCifrados().contains("Algoritmos"));
        assertFalse(banco.get("aluno@jid").getDadosCifrados().contains("6.7931"));
    }

    @Test
    void esquecerApaga() {
        servico.salvar("aluno@jid", exemplo());

        servico.esquecer("aluno@jid");

        assertFalse(servico.tem("aluno@jid"));
        assertTrue(servico.obter("aluno@jid").isEmpty());
    }

    @Test
    void reconectarSubstituiAFotoAntiga() {
        servico.salvar("aluno@jid", exemplo());
        DadosSigaa nova = new DadosSigaa(List.of(), List.of(), Map.of(), 70, List.of(),
                Instant.parse("2026-08-30T10:00:00Z"));

        servico.salvar("aluno@jid", nova);

        assertEquals(70, servico.obter("aluno@jid").get().integralizado());
        assertEquals(1, banco.size());
    }
}
