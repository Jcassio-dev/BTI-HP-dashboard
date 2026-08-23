package br.com.btihelpbot.bti_api.matricula;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SlugDocenteTest {

    private static Map<String, Docente> docentes(String... trios) {
        Map<String, Docente> m = new LinkedHashMap<>();
        for (int i = 0; i < trios.length; i += 3) {
            m.put(trios[i], new Docente(trios[i + 1], trios[i + 2]));
        }
        return m;
    }

    @Test
    void tiraAcentoMinusculaEHifeniza() {
        Map<String, String> slugs = SlugDocente.porSiape(
                docentes("1", "MAXWELL GOMES DA SILVA", "433"));

        assertEquals("maxwell-gomes-da-silva", slugs.get("1"));
    }

    @Test
    void colisaoRecebeSufixoDoDepartamento() {
        Map<String, String> slugs = SlugDocente.porSiape(docentes(
                "1544661", "GLEYDSON KLEBER LOPES DE OLIVEIRA", "79",
                "2544661", "GLEYDSON KLEBER LOPES DE OLIVEIRA", "9750"));

        assertEquals("gleydson-kleber-lopes-de-oliveira-79", slugs.get("1544661"));
        assertEquals("gleydson-kleber-lopes-de-oliveira-9750", slugs.get("2544661"));
        assertNotEquals(slugs.get("1544661"), slugs.get("2544661"));
    }

    @Test
    void semColisaoNaoGanhaSufixo() {
        Map<String, String> slugs = SlugDocente.porSiape(docentes(
                "1", "ANA MARIA", "10",
                "2", "BRUNO COSTA", "20"));

        assertEquals("ana-maria", slugs.get("1"));
        assertEquals("bruno-costa", slugs.get("2"));
    }

    @Test
    void pontuacaoEEspacoDuploViramUmHifenSo() {
        Map<String, String> slugs = SlugDocente.porSiape(
                docentes("1", "JOSE  D'ARC DE SOUZA-NETO", "5"));

        assertEquals("jose-d-arc-de-souza-neto", slugs.get("1"));
    }

    @Test
    void nomeVazioNaoGeraEntrada() {
        Map<String, String> slugs = SlugDocente.porSiape(docentes("1", "  ", "5"));

        assertEquals(0, slugs.size());
    }
}
