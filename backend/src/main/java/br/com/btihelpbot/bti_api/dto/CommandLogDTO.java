package br.com.btihelpbot.bti_api.dto;

import lombok.Data;

@Data
public class CommandLogDTO {
    private String command;
    private String userId;
    private String groupId;
}
