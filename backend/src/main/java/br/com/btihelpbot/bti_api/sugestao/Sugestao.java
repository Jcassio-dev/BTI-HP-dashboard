package br.com.btihelpbot.bti_api.sugestao;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "sugestao")
public class Sugestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private String userId;

    private String nome;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = Instant.now();
    }
}
