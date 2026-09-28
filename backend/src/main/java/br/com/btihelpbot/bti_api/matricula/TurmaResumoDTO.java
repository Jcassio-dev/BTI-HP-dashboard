package br.com.btihelpbot.bti_api.matricula;

public record TurmaResumoDTO(
        Long id,
        String codigo,
        String nome,
        String setor,
        Integer cargaHoraria,
        String ementa,
        String equivalencias,
        String preRequisito,
        String coRequisito,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long trancados,
        long totalAvaliados,
        long totalMatriculados,
        double taxaAprovacao
) {
    static TurmaResumoDTO de(Componente c) {
        Desfechos d = c.desfechos();
        return new TurmaResumoDTO(
                c.getId(), c.getCodigo(), c.getNome(), c.getSetor(), c.getCargaHoraria(),
                c.getEmenta(), c.getEquivalencias(), c.getPreRequisito(), c.getCoRequisito(),
                d.aprovados(), d.reprovadosNota(), d.reprovadosFalta(), d.trancados(),
                d.totalAvaliados(), d.totalMatriculados(), d.taxaAprovacao());
    }
}
