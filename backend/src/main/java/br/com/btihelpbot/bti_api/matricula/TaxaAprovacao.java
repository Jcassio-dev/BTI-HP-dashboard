package br.com.btihelpbot.bti_api.matricula;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Agregado pre-computado: desfechos de um docente numa disciplina,
 * entre alunos dos cursos de computacao, nos ultimos 10 semestres.
 * Reconstruido a cada ingestao.
 */
@Data
@Entity
@Table(name = "taxa_aprovacao", indexes = {
        @Index(name = "idx_taxa_componente_nome", columnList = "componenteNome"),
        @Index(name = "idx_taxa_docente_nome", columnList = "docenteNome")
})
public class TaxaAprovacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long componenteId;
    private String componenteCodigo;
    private String componenteNome;

    private String siape;
    private String docenteNome;

    private long aprovados;
    private long reprovadosNota;
    private long reprovadosFalta;
    private long trancados;

    public Desfechos desfechos() {
        return new Desfechos(aprovados, reprovadosNota, reprovadosFalta, trancados);
    }

    public void setDesfechos(Desfechos d) {
        this.aprovados = d.aprovados();
        this.reprovadosNota = d.reprovadosNota();
        this.reprovadosFalta = d.reprovadosFalta();
        this.trancados = d.trancados();
    }
}
