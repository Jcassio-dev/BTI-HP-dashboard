package br.com.btihelpbot.bti_api.matricula;

public record DestaqueDTO(
        String codigo,
        String nome,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long trancados,
        long totalAvaliados,
        long totalMatriculados,
        double taxaAprovacao
) {
    static DestaqueDTO de(Componente c) {
        Desfechos d = c.desfechos();
        return new DestaqueDTO(
                c.getCodigo(), c.getNome(),
                d.aprovados(), d.reprovadosNota(), d.reprovadosFalta(), d.trancados(),
                d.totalAvaliados(), d.totalMatriculados(), d.taxaAprovacao());
    }
}
