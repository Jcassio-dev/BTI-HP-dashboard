package br.com.btihelpbot.bti_api.matricula;

/**
 * Definicao unica dos desfechos de uma turma e das metricas derivadas.
 * Nenhum outro ponto do sistema pode calcular taxa de aprovacao ou total de matriculados.
 */
public record Desfechos(long aprovados, long reprovadosNota, long reprovadosFalta, long trancados) {

    public static final Desfechos ZERO = new Desfechos(0, 0, 0, 0);

    public long totalAvaliados() {
        return aprovados + reprovadosNota + reprovadosFalta;
    }

    public long totalMatriculados() {
        return totalAvaliados() + trancados;
    }

    public double taxaAprovacao() {
        long avaliados = totalAvaliados();
        return avaliados == 0 ? 0d : (double) aprovados / avaliados;
    }

    public Desfechos mais(Desfechos outro) {
        return new Desfechos(
                aprovados + outro.aprovados,
                reprovadosNota + outro.reprovadosNota,
                reprovadosFalta + outro.reprovadosFalta,
                trancados + outro.trancados);
    }
}
