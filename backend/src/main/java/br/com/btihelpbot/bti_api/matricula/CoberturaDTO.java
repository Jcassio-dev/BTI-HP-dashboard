package br.com.btihelpbot.bti_api.matricula;

/** Ate onde os dados de aprovacao vao: semestre mais recente e quantos foram somados. */
public record CoberturaDTO(String ultimoSemestre, int semestres) {
}
