package br.com.btihelpbot.bti_api.matricula;

/**
 * Classifica o campo "descricao" (situacao) de uma matricula em componente.
 * Regra derivada dos valores reais do dados.ufrn.br:
 *   APROVADO* / CUMPRIU  -> APROVADO
 *   REPROVADO*           -> REPROVADO
 *   resto (INDEFERIDO, TRANCADO, CANCELADO, DISPENSADO, MATRICULADO, ...) -> IGNORADO
 * Taxa de aprovacao = APROVADO / (APROVADO + REPROVADO); IGNORADO fica de fora.
 */
public final class SituacaoClassifier {

    public enum Bucket { APROVADO, REPROVADO, IGNORADO }

    private SituacaoClassifier() {}

    public static Bucket classify(String situacao) {
        if (situacao == null) {
            return Bucket.IGNORADO;
        }
        String s = situacao.trim().toUpperCase();
        if (s.startsWith("REPROVADO")) {
            return Bucket.REPROVADO;
        }
        if (s.startsWith("APROVADO") || s.equals("CUMPRIU")) {
            return Bucket.APROVADO;
        }
        return Bucket.IGNORADO;
    }
}
