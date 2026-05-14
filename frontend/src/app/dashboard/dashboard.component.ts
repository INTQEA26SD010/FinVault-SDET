import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export interface MockCard {
  id: number;
  name: string;
  number: string;
  limit: string;
  status: 'ACTIVE' | 'FROZEN';
  color: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {

  // Mock data — will be replaced with live API calls in SCRUM-16
  mockCards: MockCard[] = [
    { id: 1, name: 'Groceries',   number: '**** **** **** 1234', limit: '₹5,000 / day',  status: 'ACTIVE', color: 'primary' },
    { id: 2, name: 'Fuel',        number: '**** **** **** 5678', limit: '₹2,000 / day',  status: 'ACTIVE', color: 'success' },
    { id: 3, name: 'Entertainment', number: '**** **** **** 9012', limit: '₹1,500 / day', status: 'FROZEN', color: 'warning' },
    { id: 4, name: 'Utilities',   number: '**** **** **** 3456', limit: '₹3,000 / day',  status: 'ACTIVE', color: 'info' },
  ];
}
