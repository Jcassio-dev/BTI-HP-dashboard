package br.com.btihelpbot.bti_api.sigaa;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Foto dos dados do aluno no SIGAA num instante. E o que fica guardado; a sessao e descartada. */
public record DadosSigaa(
        List<PortalParser.Turma> turmas,
        List<Indice> indices,
        Map<String, String> institucional,
        Integer integralizado,
        Instant atualizadoEm
) {
    public record Indice(String sigla, String valor, String nome) {}
}
