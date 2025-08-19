import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  commandsToday: number = 0;
  uniqueUsersToday: number = 0;

  ngOnInit(): void {
    this.commandsToday = 5;
    this.uniqueUsersToday = 3;

    this.initChart();
  }

  initChart(): void {}
}
