package br.com.btihelpbot.bti_api.sigaa;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra o cookie de sessao do SIGAA em repouso. A chave vive em variavel de ambiente,
 * fora do banco. Isto NAO e ponta a ponta: o servidor decifra para usar a sessao.
 */
public class CofreSessao {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int TAMANHO_IV = 12;
    private static final int TAMANHO_TAG = 128;

    private final SecretKey chave;
    private final SecureRandom aleatorio = new SecureRandom();

    public CofreSessao(String chaveBase64) {
        byte[] bytes = Base64.getDecoder().decode(chaveBase64);
        if (bytes.length != 32) {
            throw new IllegalArgumentException("A chave do cofre precisa ter 32 bytes em base64");
        }
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    public static String gerarChave() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String cifrar(String texto) {
        try {
            byte[] iv = new byte[TAMANHO_IV];
            aleatorio.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG, iv));
            byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            byte[] pacote = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, pacote, 0, iv.length);
            System.arraycopy(cifrado, 0, pacote, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(pacote);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar a sessao", e);
        }
    }

    public String decifrar(String pacoteBase64) {
        try {
            byte[] pacote = Base64.getDecoder().decode(pacoteBase64);
            byte[] iv = new byte[TAMANHO_IV];
            System.arraycopy(pacote, 0, iv, 0, TAMANHO_IV);

            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAMANHO_TAG, iv));
            byte[] claro = cipher.doFinal(pacote, TAMANHO_IV, pacote.length - TAMANHO_IV);
            return new String(claro, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar a sessao", e);
        }
    }
}
