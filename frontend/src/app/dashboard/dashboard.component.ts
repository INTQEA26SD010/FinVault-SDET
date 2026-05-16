import { ChangeDetectorRef, Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { VirtualCardService, VirtualCard, TransactionResponse } from '../services/virtual-card.service';
import { AuthService, SessionUser } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  cards: VirtualCard[] = [];
  newCardLimit = 500;
  newVendorName = '';
  loading = false;
  creatingCard = false;
  /** Holds the id of the card currently being toggled (freeze/unfreeze); null when idle. */
  togglingCardId: number | null = null;
  /** Holds the id of the card currently being deleted; null when idle. */
  deletingCardId: number | null = null;
  /** Holds the id of the card currently being processed; null when idle. */
  processingCardId: number | null = null;
  errorMsg = '';

  transactions: TransactionResponse[] = [];
  txLoading = false;

  user: SessionUser | null = null;
  activeNav = 'dashboard';

  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);

  constructor(
    private cardService: VirtualCardService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getSession();
    this.fetchCards();
  }

  get userInitials(): string {
    if (!this.user?.username) return '??';
    return this.user.username.substring(0, 2).toUpperCase();
  }

  /** Derived 16-digit FinVault account card number from userId. */
  get mainCardNumber(): string {
    const uid = this.user?.userId ?? 1;
    const s1 = String(4000 + (uid % 1000)).padStart(4, '0');
    const s2 = String((uid * 7919 + 3491) % 9000 + 1000);
    const s3 = String((uid * 6271 + 7823) % 9000 + 1000);
    const s4 = String((uid * 4523 + 1847) % 9000 + 1000);
    return `${s1} ${s2} ${s3} ${s4}`;
  }

  /** Card expiry: current month + current year + 4. */
  get mainCardExpiry(): string {
    const now = new Date();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const y = String(now.getFullYear() + 4).substring(2);
    return `${m}/${y}`;
  }

  /** Time-based greeting shown in the page header. */
  get timeGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Morning';
    if (h < 17) return 'Afternoon';
    return 'Evening';
  }

  get totalLimit(): number {
    return this.cards.reduce((sum, c) => sum + (c.dailyLimit || 0), 0);
  }

  get totalSpent(): number {
    return this.cards.reduce((sum, c) => sum + (c.balance || 0), 0);
  }

  /** CSS gradient string for a virtual card at the given index (cycles through 5 themes). */
  private readonly CARD_GRADIENTS = [
    'linear-gradient(135deg, #1a1a5e 0%, #0d6efd 100%)',
    'linear-gradient(135deg, #2d1b5e 0%, #7c3aed 100%)',
    'linear-gradient(135deg, #1a3a4a 0%, #0891b2 100%)',
    'linear-gradient(135deg, #1a3d1a 0%, #059669 100%)',
    'linear-gradient(135deg, #4a1a1a 0%, #dc2626 100%)',
  ];

  cardGradient(index: number): string {
    return this.CARD_GRADIENTS[index % this.CARD_GRADIENTS.length];
  }

  /** Percentage of daily limit already spent (capped at 100). */
  spentRatio(card: VirtualCard): number {
    if (!card.dailyLimit) return 0;
    return Math.min(100, Math.round((card.balance / card.dailyLimit) * 100));
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
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMsg = 'Failed to load cards. Is the backend running?';
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  generateCard(): void {
    if (!this.user || this.newCardLimit <= 0 || !this.newVendorName.trim() || this.creatingCard) return;
    this.creatingCard = true;
    this.errorMsg = '';
    this.cardService.createCard(this.user.userId, this.newCardLimit, this.newVendorName.trim())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.creatingCard = false;
          this.newVendorName = '';
          this.fetchCards();
          this.cdr.detectChanges();
        },
        error: () => { this.errorMsg = 'Failed to create card.'; this.creatingCard = false; this.cdr.detectChanges(); }
      });
  }

  /**
   * Toggle a card's status between ACTIVE and FROZEN.
   * Guard prevents concurrent calls.
   */
  toggleCard(cardId: number): void {
    if (this.togglingCardId !== null) return;
    this.togglingCardId = cardId;
    this.errorMsg = '';
    this.cardService.toggleCard(cardId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.togglingCardId = null; this.fetchCards(); this.cdr.detectChanges(); },
        error: () => { this.errorMsg = 'Failed to toggle card status.'; this.togglingCardId = null; this.cdr.detectChanges(); }
      });
  }

  /**
   * Permanently delete a card.
   * Guard prevents concurrent calls.
   */
  deleteCard(cardId: number): void {
    if (this.deletingCardId !== null) return;
    this.deletingCardId = cardId;
    this.errorMsg = '';
    this.cardService.deleteCard(cardId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => { this.deletingCardId = null; this.fetchCards(); this.cdr.detectChanges(); },
        error: () => { this.errorMsg = 'Failed to delete card.'; this.deletingCardId = null; this.cdr.detectChanges(); }
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
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          this.processingCardId = null;
          // Backend returns 422 for DECLINED — extract the body and treat as success
          if (err.status === 422 && err.error?.status === 'DECLINED') {
            this.errorMsg = 'Transaction declined — daily limit reached.';
          } else {
            this.errorMsg = 'Transaction failed.';
          }
          this.fetchCards();
          this.cdr.detectChanges();
        }
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
          this.cdr.detectChanges();
        },
        error: () => { this.txLoading = false; this.cdr.detectChanges(); }
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

  /** Navigate to the Transaction Simulator page (no page reload — uses Angular router). */
  goToSimulator(): void {
    this.router.navigate(['/simulator']);
  }
}
