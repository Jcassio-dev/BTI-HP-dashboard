import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TemaToggle } from './tema/tema-toggle';
import { TemaService } from './tema/tema.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, TemaToggle],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly tema = inject(TemaService);
}
