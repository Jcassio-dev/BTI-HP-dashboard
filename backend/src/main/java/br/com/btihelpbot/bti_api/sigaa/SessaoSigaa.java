package br.com.btihelpbot.bti_api.sigaa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/** Sessao do SIGAA de um aluno, ligada ao numero de WhatsApp. O cookie fica cifrado. */
@Data
@Entity
@Table(name = "sessao_sigaa")
public class SessaoSigaa {

    @Id
    @Column(length = 128)
    private String jid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String cookieCifrado;

    @Column(nullable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant venceEm;
}
