package br.com.btihelpbot.bti_api.service;

import br.com.btihelpbot.bti_api.dto.AnalyticsDTO;
import br.com.btihelpbot.bti_api.dto.CommandLogDTO;
import br.com.btihelpbot.bti_api.dto.StatsSummaryDTO;
import br.com.btihelpbot.bti_api.model.CommandLog;
import br.com.btihelpbot.bti_api.repository.CommandLogRepository;
import br.com.btihelpbot.bti_api.repository.specifications.CommandLogSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MetricsService {
    private final CommandLogRepository commandLogRepository;

    public MetricsService(CommandLogRepository commandLogRepository){
        this.commandLogRepository = commandLogRepository;
    }

    public void saveLog(CommandLogDTO dto){
        CommandLog log = new CommandLog();

        log.setCommand(dto.getCommand());
        log.setUserId(dto.getUserId());
        log.setGroupId(dto.getGroupId());

        commandLogRepository.save(log);
    }

    public Page<CommandLog> findLogsByCriteria(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            String chatType,
            List<String> commands,
            Pageable pageable
    ) {
        List<Specification<CommandLog>> specifications = new ArrayList<>();


        if(startDate != null){
            specifications.add(CommandLogSpecifications.byStartDate(startDate));
        }
        if(endDate != null){
            specifications.add(CommandLogSpecifications.byEndDate(endDate));
        }

        if(chatType != null){
            specifications.add(CommandLogSpecifications.byChatType(chatType));
        }
        if(commands != null && !commands.isEmpty()){
            specifications.add(CommandLogSpecifications.byCommands(commands));
        }

        Specification<CommandLog> spec = specifications.stream().reduce(Specification::and).orElse(null);

        return commandLogRepository.findAll(spec, pageable);

    }

    public Long countCommandsByUserId(String userId) {
        return commandLogRepository.count((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), userId)
        );
    }

    public StatsSummaryDTO getStatsSummary() {
        Map<String, Long> commandCounts = commandLogRepository.countByCommand().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));


        long totalReceived = commandLogRepository.count();
        long differentUsers = commandLogRepository.countDistinctUserIds();

        return new StatsSummaryDTO(commandCounts, totalReceived, differentUsers);
    }

    public AnalyticsDTO getAnalytics() {
        List<AnalyticsDTO.OverTimePoint> overTime = commandLogRepository.analyticsOverTime().stream()
                .map(r -> new AnalyticsDTO.OverTimePoint(
                        ((Date) r[0]).toLocalDate().toString(),
                        ((Number) r[1]).longValue(),
                        ((Number) r[2]).longValue()))
                .toList();

        Map<Integer, Long> hourCounts = commandLogRepository.analyticsByHour().stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).intValue(),
                        r -> ((Number) r[1]).longValue()));
        List<AnalyticsDTO.HourPoint> byHour = IntStream.range(0, 24)
                .mapToObj(h -> new AnalyticsDTO.HourPoint(h, hourCounts.getOrDefault(h, 0L)))
                .toList();

        Object[] ct = commandLogRepository.analyticsChatType().get(0);
        long privateChats = ct[0] == null ? 0L : ((Number) ct[0]).longValue();
        long group = ct[1] == null ? 0L : ((Number) ct[1]).longValue();

        return new AnalyticsDTO(overTime, byHour, new AnalyticsDTO.ChatTypeCounts(group, privateChats));
    }
}
