package br.com.btihelpbot.bti_api.matricula;

import java.util.List;

public record TurmaDTO(
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
        double taxaAprovacao,
        List<AprovacaoDTO> professores
) {
    static TurmaDTO de(Componente c, List<AprovacaoDTO> professores) {
        Desfechos d = c.desfechos();
        return new TurmaDTO(
                c.getId(), c.getCodigo(), c.getNome(), c.getSetor(), c.getCargaHoraria(),
                c.getEmenta(), c.getEquivalencias(), c.getPreRequisito(), c.getCoRequisito(),
                d.aprovados(), d.reprovadosNota(), d.reprovadosFalta(), d.trancados(),
                d.totalAvaliados(), d.totalMatriculados(), d.taxaAprovacao(), professores);
    }
}
