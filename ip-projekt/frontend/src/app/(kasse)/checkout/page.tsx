'use client'

import {
  EmbeddedCheckout,
  EmbeddedCheckoutProvider
} from '@stripe/react-stripe-js'
import { loadStripe } from '@stripe/stripe-js'

import { CheckoutSession } from '../../components/model/checkout'
import { getCheckoutClientSecret } from '../../components/requests/CheckoutRequests'

const stripePromise = loadStripe(process.env.NEXT_PUBLIC_STRIPE_PUBLISHABLE_KEY || '')

export default function Checkout() {

  const fetchClientSecret = async () => {
    const products = JSON.parse(sessionStorage.getItem("checkoutProducts") || "[]");
    sessionStorage.removeItem("checkoutProducts");
    console.log("Fetching client secret with products checkout:", products);
    const session: CheckoutSession = {
      products: products
    };
    const res = await getCheckoutClientSecret(session);
    return res;
  }



  return (
    <div id="checkout" style={{
      marginTop: '20px',
    }}>
      <EmbeddedCheckoutProvider
        stripe={stripePromise}
        options={{ fetchClientSecret }}
        
      >
        <EmbeddedCheckout  />
      </EmbeddedCheckoutProvider>
    </div>
  )
}