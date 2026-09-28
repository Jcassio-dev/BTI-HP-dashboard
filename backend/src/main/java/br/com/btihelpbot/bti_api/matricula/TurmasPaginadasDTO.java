package br.com.btihelpbot.bti_api.matricula;

import java.util.List;

public record TurmasPaginadasDTO(
        List<TurmaResumoDTO> itens,
        int pagina,
        int tamanho,
        long total,
        int totalPaginas
) {
}
