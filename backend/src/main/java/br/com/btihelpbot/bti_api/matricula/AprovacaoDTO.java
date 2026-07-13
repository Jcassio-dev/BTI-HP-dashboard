package br.com.btihelpbot.bti_api.matricula;

public record AprovacaoDTO(
        Long componenteId,
        String componenteCodigo,
        String componenteNome,
        String docenteNome,
        long aprovados,
        long reprovados,
        long total,
        double taxa
) {
    static AprovacaoDTO from(TaxaAprovacao t) {
        return new AprovacaoDTO(
                t.getComponenteId(),
                t.getComponenteCodigo(),
                t.getComponenteNome(),
                t.getDocenteNome(),
                t.getAprovados(),
                t.getReprovados(),
                t.getTotal(),
                t.getTaxa());
    }
}
