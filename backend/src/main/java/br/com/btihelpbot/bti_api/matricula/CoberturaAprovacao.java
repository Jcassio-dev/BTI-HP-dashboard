package br.com.btihelpbot.bti_api.matricula;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/** Ate onde os dados de aprovacao vao: o semestre mais recente e quantos foram somados. Uma linha. */
@Data
@Entity
@Table(name = "cobertura_aprovacao")
public class CoberturaAprovacao {

    @Id
    private Long id = 1L;

    private String ultimoSemestre;

    private int semestres;

    private Instant atualizadoEm;
}
