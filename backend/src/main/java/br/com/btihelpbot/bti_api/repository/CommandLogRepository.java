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

    // Analytics (queries nativas Postgres). Agregamos no horario de Recife (UTC-3,
    // sem horario de verao) via offset fixo de 3h — assim nao dependemos de tzdata
    // na imagem alpine do Postgres (so usamos 'UTC', que sempre existe).
    @Query(value = """
            SELECT CAST((executed_at AT TIME ZONE 'UTC') - INTERVAL '3 hours' AS DATE) AS d,
                   COUNT(*) AS commands,
                   COUNT(DISTINCT user_id) AS users
            FROM command_logs
            GROUP BY d
            ORDER BY d
            """, nativeQuery = true)
    List<Object[]> analyticsOverTime();

    @Query(value = """
            SELECT EXTRACT(HOUR FROM (executed_at AT TIME ZONE 'UTC') - INTERVAL '3 hours') AS h,
                   COUNT(*) AS c
            FROM command_logs
            GROUP BY h
            """, nativeQuery = true)
    List<Object[]> analyticsByHour();

    @Query(value = """
            SELECT SUM(CASE WHEN group_id IS NULL THEN 1 ELSE 0 END) AS private_count,
                   SUM(CASE WHEN group_id IS NOT NULL THEN 1 ELSE 0 END) AS group_count
            FROM command_logs
            """, nativeQuery = true)
    List<Object[]> analyticsChatType();
}
