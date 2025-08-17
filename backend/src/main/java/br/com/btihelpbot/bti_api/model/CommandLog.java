package br.com.btihelpbot.bti_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "command_logs")
public class CommandLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String command;
    private String userId;
    private String groupId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant executedAt;

    @PrePersist
    public void prePersist() {
        this.executedAt = Instant.now();
    }
}
