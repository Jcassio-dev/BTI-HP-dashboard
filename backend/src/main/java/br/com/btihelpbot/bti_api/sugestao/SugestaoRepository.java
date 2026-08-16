package br.com.btihelpbot.bti_api.sugestao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {
    List<Sugestao> findTop100ByOrderByCriadoEmDesc();
}
