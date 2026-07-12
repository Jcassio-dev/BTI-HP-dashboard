package br.com.btihelpbot.bti_api.matricula;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxaAprovacaoRepository extends JpaRepository<TaxaAprovacao, Long> {

    // Fase 1: busca por disciplina (nome ou codigo), professores rankeados por taxa.
    List<TaxaAprovacao> findByComponenteNomeContainingIgnoreCaseOrderByTaxaDesc(String nome);

    List<TaxaAprovacao> findByComponenteCodigoIgnoreCaseOrderByTaxaDesc(String codigo);

    // Fase 1: busca por professor, disciplinas rankeadas por taxa.
    List<TaxaAprovacao> findByDocenteNomeContainingIgnoreCaseOrderByTaxaDesc(String nome);
}
