"use client";

import ResponsiveAppBar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";


export default function RoleLayout({
    children,
}: Readonly<{ children: React.ReactNode }>) {
    const { isAuthenticated, isLoading, user, accessRights } = useAuth();
    return (
        <>
            <ResponsiveAppBar />
            {children}
        </>
    );
}
