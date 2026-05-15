import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VirtualCardService, VirtualCard } from '../services/virtual-card.service';
import { AuthService, SessionUser } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  cards: VirtualCard[] = [];
  newCardLimit: number = 500;
  loading = false;
  errorMsg = '';
  user: SessionUser | null = null;
  activeNav = 'dashboard';

  constructor(
    private cardService: VirtualCardService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getSession();
    this.fetchCards();
  }

  get userInitials(): string {
    if (!this.user?.username) return '??';
    return this.user.username.substring(0, 2).toUpperCase();
  }

  get totalLimit(): number {
    return this.cards.reduce((sum, c) => sum + (c.dailyLimit || 0), 0);
  }

  get totalSpent(): number {
    return this.cards.reduce((sum, c) => sum + (c.balance || 0), 0);
  }

  fetchCards(): void {
    if (!this.user) return;
    this.loading = true;
    this.errorMsg = '';
    this.cardService.getCardsByUserId(this.user.userId).subscribe({
      next: (data) => { this.cards = data; this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load cards. Is the backend running?'; this.loading = false; }
    });
  }

  generateCard(): void {
    if (!this.user || this.newCardLimit <= 0) return;
    this.cardService.createCard(this.user.userId, this.newCardLimit).subscribe({
      next: () => this.fetchCards(),
      error: () => { this.errorMsg = 'Failed to create card.'; }
    });
  }

  simulatePurchase(cardId: number): void {
    this.cardService.processTransaction(cardId, 50, 'Coffee Shop').subscribe({
      next: () => this.fetchCards(),
      error: () => { this.errorMsg = 'Transaction failed.'; }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
