import axios from "axios";
import { API_BASE_URL } from "./baseUrl";

// Überprüft, ob das Cookie gültig ist
export const checkValidCookie = async (): Promise<string> => {
    try {
        const response = await axios.post(`${API_BASE_URL}/auth/cookie-validation`, {}, { 
            withCredentials: true 
        });
        return response.data;
    } catch (error) {
        throw new Error('Cookie validation failed');
    }
}