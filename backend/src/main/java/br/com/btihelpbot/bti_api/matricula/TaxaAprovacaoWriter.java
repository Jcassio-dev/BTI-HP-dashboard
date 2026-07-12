package br.com.btihelpbot.bti_api.matricula;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Troca o conteudo da tabela de forma atomica (num componente separado pro @Transactional valer). */
@Component
public class TaxaAprovacaoWriter {

    private final TaxaAprovacaoRepository repository;

    public TaxaAprovacaoWriter(TaxaAprovacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void replaceAll(List<TaxaAprovacao> rows) {
        repository.deleteAllInBatch();
        repository.saveAll(rows);
    }
}
