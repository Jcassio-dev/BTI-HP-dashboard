package br.com.btihelpbot.bti_api.matricula;

public final class SituacaoClassifier {

    public enum Categoria { APROVADO, REPROVADO_NOTA, REPROVADO_FALTA, TRANCADO, IGNORADO }

    private SituacaoClassifier() {}

    public static Categoria classificar(String situacao) {
        if (situacao == null) {
            return Categoria.IGNORADO;
        }
        String s = situacao.trim().toUpperCase();
        if (s.startsWith("APROVADO") || s.equals("CUMPRIU")) {
            return Categoria.APROVADO;
        }
        if (s.equals("REPROVADO POR FALTAS")) {
            return Categoria.REPROVADO_FALTA;
        }
        if (s.startsWith("REPROVADO")) {
            return Categoria.REPROVADO_NOTA;
        }
        if (s.equals("TRANCADO") || s.equals("DESISTENCIA") || s.equals("CANCELADO")) {
            return Categoria.TRANCADO;
        }
        return Categoria.IGNORADO;
    }
}
