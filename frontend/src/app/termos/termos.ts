import { Component } from '@angular/core';
import { VERSAO_TERMOS } from './versao';

@Component({
  selector: 'app-termos',
  standalone: true,
  templateUrl: './termos.html',
})
export class Termos {
  readonly atualizado = VERSAO_TERMOS;
}
