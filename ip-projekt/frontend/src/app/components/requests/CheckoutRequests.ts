import axios from "axios";
import { CheckoutSession } from "../model/checkout";
import { API_BASE_URL } from "./baseUrl";

const API_URL = `${API_BASE_URL}/checkout`;

export const getCheckoutClientSecret = async (data: CheckoutSession):Promise<string> => {

    const response = await axios.post(`${API_URL}/create-checkout-session`, data, {withCredentials: true});
    return response.data;
    
}