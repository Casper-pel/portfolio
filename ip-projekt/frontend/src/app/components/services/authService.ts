import { API_BASE_URL } from "../requests/baseUrl";

export const AuthService = {
    async logout(): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/logout`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                console.log('Logout successful');
                return true;
            } else {
                console.error('Logout failed:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Logout error:', error);
            return false;
        }
    },

    async updatePassword(currentPassword: string, newPassword: string): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/auth/update-password`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    currentPassword,
                    newPassword
                })
            });

            if (response.ok) {
                console.log('Password updated successfully');
                return true;
            } else {
                console.error('Failed to update password:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error updating password:', error);
            return false;
        }
    }
};
