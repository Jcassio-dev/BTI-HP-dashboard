package br.com.btihelpbot.bti_api.matricula;

import java.util.List;

public record ProfessorDTO(
        String slug,
        String nome,
        long aprovados,
        long reprovadosNota,
        long reprovadosFalta,
        long trancados,
        long totalAvaliados,
        long totalMatriculados,
        double taxaAprovacao,
        List<AprovacaoDTO> turmas
) {
    static ProfessorDTO de(String slug, String nome, Desfechos d, List<AprovacaoDTO> turmas) {
        return new ProfessorDTO(
                slug, nome,
                d.aprovados(), d.reprovadosNota(), d.reprovadosFalta(), d.trancados(),
                d.totalAvaliados(), d.totalMatriculados(), d.taxaAprovacao(), turmas);
    }
}
