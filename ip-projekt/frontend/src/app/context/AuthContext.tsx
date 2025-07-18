"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { CookieService } from "../components/services/cookieService";
import { useRouter } from "next/navigation";
import { User } from "../components/model/user";
import { API_BASE_URL } from "../components/requests/baseUrl";
import { UserService } from "../components/services/userService";
import { AuthService } from "../components/services/authService";

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    accessRights: string[];
    isLoading: boolean;
    login: (user: User, accessRights: string[]) => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [accessRights, setAccessRights] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const router = useRouter();

    const refreshAuth = async () => {
        try {
            setIsLoading(true);

            const isAuth = await CookieService.isAuthenticated();
            setIsAuthenticated(isAuth);

            if (!isAuth) {
                // Falls nicht authentifiziert, leite zur Login-Seite weiter
                setUser(null);
                setAccessRights([]);
                router.push("/login");
                return;
            }

            // Wenn authentifiziert, hole die Access Rights
            const rights = await CookieService.getAccessRights();

            if (rights && Array.isArray(rights)) {
                setAccessRights(rights);
            } else {
                setAccessRights([]);
            }

            // Hole die Benutzerdaten des eingeloggten Nutzers
            const currentUser = await UserService.getCurrentUser();

            if (currentUser) {
                setUser(currentUser);
            } else {
                setUser(null);
            }

        } catch (error) {
            console.error("Auth refresh failed:", error);
            setIsAuthenticated(false);
            setUser(null);
            setAccessRights([]);
            // Verwende window.location für eine vollständige Umleitung
            window.location.href = "/login";
        } finally {
            setIsLoading(false);
        }
    };

    const login = (userData: User, userRights: string[]) => {
        setUser(userData);
        setAccessRights(userRights);
        setIsAuthenticated(true);
        setIsLoading(false);
    };

    const logout = async () => {
        try {
            // Zuerst den lokalen State zurücksetzen
            setUser(null);
            setAccessRights([]);
            setIsAuthenticated(false);

            // Dann den Server-seitigen Logout durchführen
            await AuthService.logout();

            // Zur Login-Seite weiterleiten
            router.push("/login");
        } catch (error) {
            console.error("Logout failed:", error);
            // Auch bei Fehlern den lokalen State zurücksetzen
            setUser(null);
            setAccessRights([]);
            setIsAuthenticated(false);
            // Fallback: Direkte Umleitung
            window.location.href = "/login";
        }
    };

    useEffect(() => {
        (async () => {
            await refreshAuth();
        })();
    }, []);

    const contextValue: AuthContextType = {
        user,
        accessRights,
        isLoading,
        isAuthenticated,
        login,
        logout
    };

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
}

// Custom Hook to use AuthContext
export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}

export default AuthContext;
export type { User, AuthContextType };