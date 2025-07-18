import { User } from "../model/user";
import { API_BASE_URL } from "../requests/baseUrl";

export const UserService = {
    async getCurrentUser(): Promise<User | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/current`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const userData = await response.json();
                return userData;
            } else {
                console.error('Failed to get current user:', response.statusText);
                return null;
            }
        } catch (error) {
            console.error('Error getting current user:', error);
            return null;
        }
    }
};
