import {Decimal} from "decimal.js";

export interface ProductDto {
    productName: string;
    productId: string;
    productDescription: string;
    listPrice: number;  // Mindestwert: 1
    costPrice: number;   // Mindestwert: 1
    upcCode: string;
    created: number;  // int32 (Unix-Timestamp oder andere Ganzzahlzeitrepräsentation)
    updated: number;  // int32 (Unix-Timestamp oder andere Ganzzahlzeitrepräsentation)
    active: boolean;
    currency: string;  // z. B. "USD", "EUR"
    taxIncludedInPrice: boolean;
    priceId: string; // ID des Preises, falls vorhanden
}

export interface Product {
    productName: string;
    productId: string;
    productDescription: string;
    listPrice: number;  // Mindestwert: 1
    costPrice: number;   // Mindestwert: 1
    upcCode: string;
    created: Date;  // int32 (Unix-Timestamp oder andere Ganzzahlzeitrepräsentation)
    updated: Date;  // int32 (Unix-Timestamp oder andere Ganzzahlzeitrepräsentation)
    active: boolean;
    currency: string;  // z. B. "USD", "EUR"
    taxIncludedInPrice: boolean;
    priceId: string; // ID des Preises, falls vorhanden
}

export interface RespBestSellingProductDto {
  productName: string;
  productId: string;
  dates: string[]; // ISO date strings
  totalQuantity: number;
}


export interface PriceHistory {
    listPrice: Decimal;
    costPrice: Decimal;
    changedDate: Date;
    active: boolean;
    currency: string;
}

export interface PriceHistoryWithProductId {
    priceHistory: PriceHistory[];
    productId: string;
}