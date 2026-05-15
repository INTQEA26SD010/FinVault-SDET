import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { VirtualCardService, VirtualCard, TransactionResponse } from '../services/virtual-card.service';
import { AuthService, SessionUser } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  cards: VirtualCard[] = [];
  newCardLimit = 500;
  loading = false;
  creatingCard = false;
  /** Holds the id of the card currently being processed; null when idle. */
  processingCardId: number | null = null;
  errorMsg = '';

  transactions: TransactionResponse[] = [];
  txLoading = false;

  user: SessionUser | null = null;
  activeNav = 'dashboard';

  private readonly destroyRef = inject(DestroyRef);

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

  /** Switch active tab and trigger data-fetch for tabs that need it. */
  setActiveNav(nav: string): void {
    this.activeNav = nav;
    if (nav === 'transactions') {
      this.fetchAllTransactions();
    }
  }

  fetchCards(): void {
    if (!this.user) return;
    this.loading = true;
    this.errorMsg = '';
    this.cardService.getCardsByUserId(this.user.userId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.cards = data;
          this.loading = false;
          // Refresh transaction list if that tab is active
          if (this.activeNav === 'transactions') {
            this.fetchAllTransactions();
          }
        },
        error: () => {
          this.errorMsg = 'Failed to load cards. Is the backend running?';
          this.loading = false;
        }
      });
  }

  generateCard(): void {
    if (!this.user || this.newCardLimit <= 0 || this.creatingCard) return;
    this.creatingCard = true;
    this.errorMsg = '';
    this.cardService.createCard(this.user.userId, this.newCardLimit)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.creatingCard = false; this.fetchCards(); },
        error: () => { this.errorMsg = 'Failed to create card.'; this.creatingCard = false; }
      });
  }

  /**
   * Simulate a $50 purchase against the given card.
   * stopPropagation prevents the click from reaching any ancestor handler.
   * The processingCardId guard prevents concurrent calls.
   */
  simulatePurchase(cardId: number, event: MouseEvent): void {
    event.stopPropagation();
    if (this.processingCardId !== null) return;
    this.processingCardId = cardId;
    this.errorMsg = '';
    this.cardService.processTransaction(cardId, 50, 'Coffee Shop')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.processingCardId = null;
          if (res.status === 'DECLINED') {
            this.errorMsg = 'Transaction declined — daily limit reached.';
          }
          this.fetchCards();
        },
        error: () => { this.errorMsg = 'Transaction failed.'; this.processingCardId = null; }
      });
  }

  /** Fetch transactions for every card in parallel using forkJoin, then merge and sort by date. */
  fetchAllTransactions(): void {
    if (!this.user || this.txLoading) return;
    if (this.cards.length === 0) { this.transactions = []; return; }
    this.txLoading = true;
    const requests = this.cards.map(c =>
      this.cardService.getTransactionsByCardId(c.id)
        .pipe(catchError(() => of([] as TransactionResponse[])))
    );
    forkJoin(requests)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (results) => {
          this.transactions = (results as TransactionResponse[][])
            .flat()
            .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
          this.txLoading = false;
        },
        error: () => { this.txLoading = false; }
      });
  }

  /** Return the last 4 digits of the card number for a given card id. */
  getCardLastFour(cardId: number): string {
    const card = this.cards.find(c => c.id === cardId);
    return card ? card.cardNumber.slice(-4) : '????';
  }

  logout(): void {
    this.authService.logout();
  }
}
