import { Pipe, PipeTransform } from '@angular/core';
import { tituloCase } from './titulo';

@Pipe({ name: 'titulo', standalone: true, pure: true })
export class TituloPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    return tituloCase(valor);
  }
}
