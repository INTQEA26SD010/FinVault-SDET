import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VirtualCardService, VirtualCard } from '../services/virtual-card.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  cards: VirtualCard[] = [];
  newCardLimit: number = 500;
  loading = false;
  errorMsg = '';

  private readonly userId = 1; // hardcoded until login session is implemented

  constructor(private cardService: VirtualCardService) {}

  ngOnInit(): void {
    this.fetchCards();
  }

  fetchCards(): void {
    this.loading = true;
    this.cardService.getCardsByUserId(this.userId).subscribe({
      next: (data) => { this.cards = data; this.loading = false; },
      error: (err) => { this.errorMsg = 'Failed to load cards. Is the backend running?'; this.loading = false; console.error(err); }
    });
  }

  generateCard(): void {
    if (this.newCardLimit <= 0) return;
    this.cardService.createCard(this.userId, this.newCardLimit).subscribe({
      next: () => this.fetchCards(),
      error: (err) => { this.errorMsg = 'Failed to create card.'; console.error(err); }
    });
  }

  simulatePurchase(cardId: number): void {
    this.cardService.processTransaction(cardId, 50, 'Coffee').subscribe({
      next: () => this.fetchCards(),
      error: (err) => { this.errorMsg = 'Transaction failed.'; console.error(err); }
    });
  }
}
