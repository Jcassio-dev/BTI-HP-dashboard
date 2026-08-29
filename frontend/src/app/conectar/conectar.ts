import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api/api.service';

type Estado = 'form' | 'enviando' | 'ok' | 'erro';

@Component({
  selector: 'app-conectar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './conectar.html',
  styleUrl: './conectar.css',
})
export class Conectar {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly usuario = signal('');
  readonly senha = signal('');
  readonly verSenha = signal(false);
  readonly aceito = signal(false);
  readonly estado = signal<Estado>('form');
  readonly erro = signal('');

  private readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly semToken = !this.token;
  readonly enviando = computed(() => this.estado() === 'enviando');

  enviar(): void {
    if (this.enviando()) return;
    const u = this.usuario().trim();
    const s = this.senha();
    if (!u || !s) {
      this.erro.set('Preencha login e senha.');
      return;
    }
    if (!this.aceito()) {
      this.erro.set('Aceite os termos de uso para continuar.');
      return;
    }
    this.estado.set('enviando');
    this.erro.set('');
    this.api.conectarSigaa(this.token, u, s).subscribe({
      next: () => {
        this.senha.set('');
        this.estado.set('ok');
      },
      error: (e) => {
        this.senha.set('');
        this.estado.set('erro');
        this.erro.set(this.mensagem(e?.status));
      },
    });
  }

  tentarDeNovo(): void {
    this.estado.set('form');
    this.erro.set('');
  }

  private mensagem(status: number): string {
    if (status === 401) return 'Login ou senha do SIGAA incorretos. Confira e tente de novo.';
    if (status === 403) return 'O SIGAA recusou o acesso desta conta.';
    if (status === 410) return 'Este link expirou. Peça outro com !conectar no WhatsApp.';
    if (status === 502) return 'O SIGAA não respondeu agora. Tente de novo em instantes.';
    return 'Não consegui conectar agora. Tente de novo em instantes.';
  }
}
