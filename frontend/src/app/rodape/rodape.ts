import { Component } from '@angular/core';

@Component({
  selector: 'bti-rodape',
  standalone: true,
  template: `
    <p class="t-apoio max-w-[70ch]">
      A taxa é aprovados ÷ (aprovados + reprovados); quem trancou entra na largura da faixa, não na
      conta. Turmas pequenas oscilam muito, 100% com 17 alunos diz menos que 95% com 174. Taxa de
      aprovação é histórico de turma, não medida de qualidade de ensino: disciplina, horário e
      perfil da turma pesam tanto quanto o professor.
    </p>
  `,
})
export class Rodape {}
