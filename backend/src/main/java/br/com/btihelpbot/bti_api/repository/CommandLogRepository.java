package br.com.btihelpbot.bti_api.repository;


import br.com.btihelpbot.bti_api.model.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CommandLogRepository extends JpaRepository<CommandLog, Long>, JpaSpecificationExecutor<CommandLog> {
    @Query("SELECT c.command, COUNT(c) FROM CommandLog c GROUP BY c.command")
    List<Object[]> countByCommand();

    @Query("SELECT COUNT(DISTINCT c.userId) FROM CommandLog c")
    long countDistinctUserIds();


}
