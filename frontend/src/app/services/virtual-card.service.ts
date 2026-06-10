import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface VirtualCard {
  id: number;
  cardNumber: string;
  cvv: string;
  dailyLimit: number;
  balance: number;
  status: string;
  vendorName: string;
  userId: number;
}

export interface TransactionResponse {
  id: number;
  cardId: number;
  amount: number;
  merchantName: string;
  timestamp: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class VirtualCardService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /** Fetch all virtual cards for a given user */
  getCardsByUserId(userId: number): Observable<VirtualCard[]> {
    return this.http.get<VirtualCard[]>(`${this.baseUrl}/cards/user/${userId}`);
  }

  /** Create a new virtual card with a vendor name and daily limit */
  createCard(userId: number, dailyLimit: number, vendorName: string): Observable<VirtualCard> {
    return this.http.post<VirtualCard>(`${this.baseUrl}/cards`, { userId, dailyLimit, vendorName });
  }

  /** Toggle a card's status between ACTIVE and FROZEN */
  toggleCard(cardId: number): Observable<VirtualCard> {
    return this.http.put<VirtualCard>(`${this.baseUrl}/cards/${cardId}/toggle`, {});
  }

  /** Permanently delete a card */
  deleteCard(cardId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/cards/${cardId}`);
  }

  /** Process a transaction against a card */
  processTransaction(cardId: number, amount: number, merchantName: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.baseUrl}/transactions`, {
      cardId,
      amount,
      merchantName
    });
  }

  /** Fetch all transactions for a given card, ordered by most recent first */
  getTransactionsByCardId(cardId: number): Observable<TransactionResponse[]> {
    return this.http.get<TransactionResponse[]>(`${this.baseUrl}/transactions/card/${cardId}`);
  }
}
