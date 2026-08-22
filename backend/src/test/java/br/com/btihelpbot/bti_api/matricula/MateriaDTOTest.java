package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MateriaDTOTest {

    @Test
    void taxaDaDisciplinaIgnoraTrancadosNoDenominador() {
        Componente c = new Componente();
        c.setId(1L);
        c.setCodigo("MAT0031");
        c.setDesfechos(new Desfechos(38, 4, 0, 10));

        MateriaDTO dto = MateriaDTO.from(c);

        assertEquals(42, dto.totalAvaliados());
        assertEquals(52, dto.totalMatriculados());
        assertEquals(0.9047619047619048d, dto.taxaAprovacao(), 1e-9);
    }
}
