package br.com.btihelpbot.bti_api.service;

import br.com.btihelpbot.bti_api.dto.CommandLogDTO;
import br.com.btihelpbot.bti_api.model.CommandLog;
import br.com.btihelpbot.bti_api.repository.CommandLogRepository;
import br.com.btihelpbot.bti_api.repository.specifications.CommandLogSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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



}
