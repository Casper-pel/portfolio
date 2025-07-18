export interface CheckoutSession {
    products: CheckoutProducts[];
}

export interface CheckoutProducts{
    productId: string;
    quantity: number;
    price: string;
}