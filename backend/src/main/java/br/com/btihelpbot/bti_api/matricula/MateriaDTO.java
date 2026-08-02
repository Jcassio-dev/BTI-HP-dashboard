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
        long aprovado,
        long reprovadoNota,
        long reprovadoFalta,
        long trancado,
        long total
) {
    static MateriaDTO from(Componente c) {
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
                c.getAprovado(),
                c.getReprovadoNota(),
                c.getReprovadoFalta(),
                c.getTrancado(),
                c.getTotal());
    }
}
