package br.com.btihelpbot.bti_api.matricula;

public record MateriaDTO(
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
    static MateriaDTO from(Componente c) {
        Desfechos d = c.desfechos();
        return new MateriaDTO(
                c.getId(),
                c.getCodigo(),
                c.getNome(),
                c.getSetor(),
                c.getCargaHoraria(),
                c.getEmenta(),
                c.getEquivalencias(),
                c.getPreRequisito(),
                c.getCoRequisito(),
                d.aprovados(),
                d.reprovadosNota(),
                d.reprovadosFalta(),
                d.trancados(),
                d.totalAvaliados(),
                d.totalMatriculados(),
                d.taxaAprovacao());
    }
}
