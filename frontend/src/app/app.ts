import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { TemaToggle } from './tema/tema-toggle';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TemaToggle],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly router = inject(Router);

  private readonly url = signal(this.router.url);
  readonly noBot = computed(() => this.url().startsWith('/bot'));

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.url.set(e.urlAfterRedirects));
  }
}
