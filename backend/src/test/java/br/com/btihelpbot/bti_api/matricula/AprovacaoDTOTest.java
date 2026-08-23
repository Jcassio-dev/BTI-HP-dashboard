package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AprovacaoDTOTest {

    @Test
    void exportaOsDoisNumerosSeparadosQuandoHaTrancados() {
        TaxaAprovacao t = new TaxaAprovacao();
        t.setComponenteCodigo("MAT0031");
        t.setDocenteNome("PROF A");
        t.setDocenteSlug("prof-a");
        t.setDesfechos(new Desfechos(38, 4, 0, 10));

        AprovacaoDTO dto = AprovacaoDTO.from(t);

        assertEquals("prof-a", dto.docenteSlug());
        assertEquals(38, dto.aprovados());
        assertEquals(4, dto.reprovadosNota());
        assertEquals(0, dto.reprovadosFalta());
        assertEquals(10, dto.trancados());
        assertEquals(42, dto.totalAvaliados());
        assertEquals(52, dto.totalMatriculados());
        assertEquals(38d / 42d, dto.taxaAprovacao(), 1e-9);
    }
}
