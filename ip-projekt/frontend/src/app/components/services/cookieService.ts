import { API_BASE_URL } from "../requests/baseUrl";

export const CookieService = {
    async checkValidCookie(): Promise<Response> {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/cookie-validation`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Cookie validation failed with status: ${response.status}`);
            }
            return response;
        } catch (error) {
            throw new Error('Cookie validation failed');
        }
    },

    // Methode um zu überprüfen, ob der User eingeloggt ist, false falls nicht
    async isAuthenticated(): Promise<boolean> {
        try {
            const result = await this.checkValidCookie();
            if (result.status === 200) {
                return true;
            }
            return false;
        } catch (error) {
            return false;
        }
    },

    // Methode um Rechte des Users zu erhalten, Array von Strings
    async getAccessRights(): Promise<string[]> {
        try {
            const response = await CookieService.checkValidCookie();

            if (response.ok) {
                const data = await response.json();

                // Prüfe ob accessRights existiert und ein Array ist
                if (data && Array.isArray(data.accessRights)) {
                    return data.accessRights;
                } else {
                    return [];
                }
            } else {
                return [];
            }
        } catch (error) {
            return [];
        }
    }
};