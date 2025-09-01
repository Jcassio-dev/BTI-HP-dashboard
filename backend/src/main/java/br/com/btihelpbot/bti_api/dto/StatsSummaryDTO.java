package br.com.btihelpbot.bti_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsSummaryDTO {
    private Map<String, Long> counts;

    private long totalReceived;

    private long differentUsers;
}