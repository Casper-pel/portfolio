export interface CouponDto {
    id: string;
    name: string;
    amountOff: number;
    currency: string;
    duration: string; // e.g., "once", "repeating", "forever"
    percentOff: number;
}


export interface NewCouponDto {
    name: string;
    amountOff: number;
    currency: string;
    duration: string; // e.g., "once", "repeating", "forever"
    percentOff: number;
}
