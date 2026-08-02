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

    private long aprovado;
    private long reprovadoNota;
    private long reprovadoFalta;
    private long trancado;
    private long total;
}
