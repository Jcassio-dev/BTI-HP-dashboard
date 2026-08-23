package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NomesCanonicosTest {

    @Test
    void escolheAGrafiaAcentuadaQuandoAMesmaDisciplinaApareceDasDuasFormas() {
        NomesCanonicos n = NomesCanonicos.de(List.of(
                "CALCULO DIFERENCIAL E INTEGRAL I",
                "CÁLCULO DIFERENCIAL E INTEGRAL I"));

        assertEquals("CÁLCULO DIFERENCIAL E INTEGRAL I",
                n.melhor("CALCULO DIFERENCIAL E INTEGRAL I"));
        assertEquals("CÁLCULO DIFERENCIAL E INTEGRAL I",
                n.melhor("CÁLCULO DIFERENCIAL E INTEGRAL I"));
    }

    @Test
    void escolheAVarianteComMaisAcentos() {
        NomesCanonicos n = NomesCanonicos.de(List.of(
                "INTRODUCAO AS TECNICAS DE PROGRAMACAO",
                "INTRODUÇÃO AS TECNICAS DE PROGRAMACAO",
                "INTRODUÇÃO ÀS TÉCNICAS DE PROGRAMAÇÃO"));

        assertEquals("INTRODUÇÃO ÀS TÉCNICAS DE PROGRAMAÇÃO",
                n.melhor("INTRODUCAO AS TECNICAS DE PROGRAMACAO"));
    }

    @Test
    void nomeSemVarianteVoltaIntacto() {
        NomesCanonicos n = NomesCanonicos.de(List.of("ESTRUTURAS DE DADOS BASICAS I"));

        assertEquals("ESTRUTURAS DE DADOS BASICAS I", n.melhor("ESTRUTURAS DE DADOS BASICAS I"));
        assertEquals("DISCIPLINA NUNCA VISTA", n.melhor("DISCIPLINA NUNCA VISTA"));
    }

    @Test
    void naoInventaAcentoQuandoNenhumaVarianteTem() {
        NomesCanonicos n = NomesCanonicos.de(List.of("CALCULO NUMERICO", "CALCULO NUMERICO"));

        assertEquals("CALCULO NUMERICO", n.melhor("CALCULO NUMERICO"));
    }

    @Test
    void toleraNuloEBranco() {
        NomesCanonicos n = NomesCanonicos.de(List.of("ALGORITMOS"));

        assertNull(n.melhor(null));
        assertEquals("  ", n.melhor("  "));
    }
}
