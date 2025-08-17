package br.com.btihelpbot.bti_api.repository;


import br.com.btihelpbot.bti_api.model.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface CommandLogRepository extends JpaRepository<CommandLog, Long>, JpaSpecificationExecutor<CommandLog> {
}
