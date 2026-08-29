package br.com.btihelpbot.bti_api.sigaa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/** Foto cifrada dos dados do aluno. Sem sessao guardada: so o resultado da ultima coleta. */
@Data
@Entity
@Table(name = "snapshot_sigaa")
public class SnapshotSigaa {

    @Id
    @Column(length = 128)
    private String jid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dadosCifrados;

    @Column(nullable = false)
    private Instant atualizadoEm;
}
