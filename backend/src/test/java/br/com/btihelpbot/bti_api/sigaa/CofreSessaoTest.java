package br.com.btihelpbot.bti_api.sigaa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CofreSessaoTest {

    private static final String CHAVE = CofreSessao.gerarChave();
    private static final String OUTRA = CofreSessao.gerarChave();

    @Test
    void cifraEDecifraDeVolta() {
        CofreSessao cofre = new CofreSessao(CHAVE);

        String pacote = cofre.cifrar("JSESSIONID=abc123");

        assertEquals("JSESSIONID=abc123", cofre.decifrar(pacote));
    }

    @Test
    void oMesmoTextoNuncaGeraOMesmoPacote() {
        CofreSessao cofre = new CofreSessao(CHAVE);

        assertNotEquals(cofre.cifrar("igual"), cofre.cifrar("igual"));
    }

    @Test
    void oPacoteNaoContemOTextoEmClaro() {
        CofreSessao cofre = new CofreSessao(CHAVE);

        assertEquals(false, cofre.cifrar("JSESSIONID=segredo").contains("segredo"));
    }

    @Test
    void chaveErradaNaoDecifra() {
        String pacote = new CofreSessao(CHAVE).cifrar("JSESSIONID=abc123");

        assertThrows(IllegalStateException.class, () -> new CofreSessao(OUTRA).decifrar(pacote));
    }

    @Test
    void pacoteAdulteradoNaoDecifra() {
        CofreSessao cofre = new CofreSessao(CHAVE);
        String pacote = cofre.cifrar("JSESSIONID=abc123");
        String adulterado = pacote.substring(0, pacote.length() - 4) + "AAAA";

        assertThrows(IllegalStateException.class, () -> cofre.decifrar(adulterado));
    }
}
