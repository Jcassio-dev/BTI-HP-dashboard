package br.com.btihelpbot.bti_api.matricula;

/** Uma linha do CSV matriculas-componentes (so os campos que usamos). */
public record MatriculaRow(long idTurma, String discente, String idCurso, String situacao) {}
