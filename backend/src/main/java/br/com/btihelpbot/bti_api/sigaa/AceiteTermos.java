package br.com.btihelpbot.bti_api.sigaa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/** Registro de que um aluno aceitou os termos: qual versao, de qual ip, quando. Auditoria. */
@Data
@Entity
@Table(name = "aceite_termos")
public class AceiteTermos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String jid;

    @Column(nullable = false)
    private String versao;

    @Column(length = 64)
    private String ip;

    @Column(nullable = false)
    private Instant aceitoEm;
}
