package br.com.btihelpbot.bti_api.matricula;

public record AprovacaoDTO(
        Long componenteId,
        String componenteCodigo,
        String componenteNome,
        String docenteNome,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long trancados,
        long totalAvaliados,
        long totalMatriculados,
        double taxaAprovacao
) {
    static AprovacaoDTO from(TaxaAprovacao t) {
        Desfechos d = t.desfechos();
        return new AprovacaoDTO(
                t.getComponenteId(),
                t.getComponenteCodigo(),
                t.getComponenteNome(),
                t.getDocenteNome(),
                d.aprovados(),
                d.reprovadosNota(),
                d.reprovadosFalta(),
                d.trancados(),
                d.totalAvaliados(),
                d.totalMatriculados(),
                d.taxaAprovacao());
    }
}
