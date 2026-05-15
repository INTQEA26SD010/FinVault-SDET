import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VirtualCard {
  id: number;
  cardNumber: string;
  cvv: string;
  dailyLimit: number;
  balance: number;
  userId: number;
}

export interface TransactionResponse {
  id: number;
  cardId: number;
  amount: number;
  merchantName: string;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class VirtualCardService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  /** Fetch all virtual cards for a given user */
  getCardsByUserId(userId: number): Observable<VirtualCard[]> {
    return this.http.get<VirtualCard[]>(`${this.baseUrl}/cards/user/${userId}`);
  }

  /** Create a new virtual card */
  createCard(userId: number, dailyLimit: number): Observable<VirtualCard> {
    return this.http.post<VirtualCard>(`${this.baseUrl}/cards`, { userId, dailyLimit });
  }

  /** Process a transaction against a card */
  processTransaction(cardId: number, amount: number, merchantName: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.baseUrl}/transactions`, {
      cardId,
      amount,
      merchantName
    });
  }
}
