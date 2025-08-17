package br.com.btihelpbot.bti_api.repository.specifications;

import br.com.btihelpbot.bti_api.model.CommandLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.List;

public class CommandLogSpecifications {

    public static Specification<CommandLog> byStartDate(ZonedDateTime startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo((root.get("executedAt")), startDate);
    }

    public static Specification<CommandLog> byEndDate(ZonedDateTime endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo((root.get("executedAt")), endDate);
    }

    public static Specification<CommandLog> byChatType(String chatType) {

        return (root, query, criteriaBuilder) -> {
            if ("GROUP".equalsIgnoreCase(chatType)) {
                return criteriaBuilder.isNotNull(root.get("groupId"));
            } else if ("USER".equalsIgnoreCase(chatType)) {
                return criteriaBuilder.isNull(root.get("groupId"));
            }
            return criteriaBuilder.conjunction();
        };

    }

    public static Specification<CommandLog> byCommands(List<String> commands) {
        return (root, query, criteriaBuilder) ->
                root.get("commandName").in(commands);
    }


}
