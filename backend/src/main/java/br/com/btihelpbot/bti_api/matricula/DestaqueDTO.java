package br.com.btihelpbot.bti_api.matricula;

public record DestaqueDTO(
        String codigo,
        String nome,
        long totalMatriculados,
        double taxaAprovacao
) {
    static DestaqueDTO de(Componente c) {
        Desfechos d = c.desfechos();
        return new DestaqueDTO(c.getCodigo(), c.getNome(), d.totalMatriculados(), d.taxaAprovacao());
    }
}
