package br.com.btihelpbot.bti_api.matricula;

import java.util.List;

public record BuscaDTO(List<AprovacaoDTO> disciplinas, List<AprovacaoDTO> professores) {}
