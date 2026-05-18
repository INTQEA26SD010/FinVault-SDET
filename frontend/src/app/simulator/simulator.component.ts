import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import {
  VirtualCardService,
  VirtualCard,
  TransactionResponse,
} from '../services/virtual-card.service';
import { AuthService, SessionUser } from '../services/auth.service';

@Component({
  selector: 'app-simulator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './simulator.component.html',
  styleUrl: './simulator.component.css',
})
export class SimulatorComponent implements OnInit {
  // ── Card state ─────────────────────────────────────────────────────
  cards: VirtualCard[] = [];
  activeCards: VirtualCard[] = [];
  loadingCards = false;

  // ── Form fields ────────────────────────────────────────────────────
  selectedCardId: number | null = null;
  amount: number | null = null;
  merchantName = '';

  // ── Submission state ───────────────────────────────────────────────
  isSubmitting = false;
  alertType: 'success' | 'danger' | '' = '';
  alertMessage = '';
  lastTransaction: TransactionResponse | null = null;

  // ── Session ────────────────────────────────────────────────────────
  user: SessionUser | null = null;
  sidebarOpen = false;

  private readonly cdr = inject(ChangeDetectorRef);

  constructor(
    private cardService: VirtualCardService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getSession();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }
    this.fetchCards();
  }

  get userInitials(): string {
    if (!this.user?.username) return '??';
    return this.user.username.substring(0, 2).toUpperCase();
  }

  /** Fetch the user's cards and filter to ACTIVE ones for the dropdown. */
  fetchCards(): void {
    this.loadingCards = true;
    this.cardService.getCardsByUserId(this.user!.userId).subscribe({
      next: (cards) => {
        this.cards = cards;
        this.activeCards = cards.filter((c) => c.status === 'ACTIVE');
        if (this.activeCards.length > 0 && this.selectedCardId === null) {
          this.selectedCardId = this.activeCards[0].id;
        }
        this.loadingCards = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingCards = false;
        this.alertType = 'danger';
        this.alertMessage = 'Failed to load cards. Is the backend running?';
        this.cdr.detectChanges();
      },
    });
  }

  /** Display label for the card select dropdown: "VendorName •••• XXXX". */
  cardLabel(card: VirtualCard): string {
    const last4 = card.cardNumber.slice(-4);
    const vendor = card.vendorName?.trim() || 'Card';
    return `${vendor}  ••••  ${last4}`;
  }

  /** Submit the test transaction to the backend. */
  onSubmit(): void {
    if (
      !this.selectedCardId ||
      this.amount === null ||
      this.amount <= 0 ||
      !this.merchantName.trim()
    )
      return;

    this.isSubmitting = true;
    this.alertType = '';
    this.alertMessage = '';

    this.cardService
      .processTransaction(
        this.selectedCardId,
        this.amount,
        this.merchantName.trim()
      )
      .subscribe({
        next: (res) => {
          this.lastTransaction = res;
          this.isSubmitting = false;
          if (res.status === 'SUCCESS') {
            this.alertType = 'success';
            this.alertMessage = `Transaction APPROVED — $${res.amount.toFixed(
              2
            )} charged to "${res.merchantName}" was recorded successfully.`;
          } else {
            this.alertType = 'danger';
            this.alertMessage = `Transaction DECLINED — $${res.amount.toFixed(
              2
            )} to "${res.merchantName}" exceeds the card's daily limit.`;
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isSubmitting = false;
          // Backend may return 422 with a DECLINED body
          const body = err?.error;
          if (err?.status === 422 && body?.status === 'DECLINED') {
            this.alertType = 'danger';
            this.alertMessage = `Transaction DECLINED — $${
              this.amount!.toFixed(2)
            } to "${this.merchantName}" exceeds the card's daily limit.`;
          } else {
            this.alertType = 'danger';
            this.alertMessage =
              body?.message ||
              body?.error ||
              'Transaction failed — backend unreachable or an unexpected error occurred.';
          }
          this.cdr.detectChanges();
        },
      });
  }

  dismissAlert(): void {
    this.alertType = '';
    this.alertMessage = '';
  }

  toggleSidebar(): void { this.sidebarOpen = !this.sidebarOpen; }
  closeSidebar(): void  { this.sidebarOpen = false; }

  logout(): void {
    this.authService.logout();
  }
}
