package br.com.btihelpbot.bti_api.matricula;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "componente")
public class Componente {

    @Id
    private Long id;

    private String codigo;
    private String nome;
    private String setor;
    private Integer cargaHoraria;

    @Column(columnDefinition = "TEXT")
    private String ementa;

    @Column(columnDefinition = "TEXT")
    private String equivalencias;

    @Column(columnDefinition = "TEXT")
    private String preRequisito;

    @Column(columnDefinition = "TEXT")
    private String coRequisito;

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
