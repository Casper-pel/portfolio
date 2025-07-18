"use client";

import { Box, Typography, Container } from '@mui/material';
import { useEffect } from 'react';
import CheckoutCard from './components/cards/CheckoutCard';
import DashboardCard from './components/cards/DashboardCard';
import { useAuth } from './context/AuthContext';

export default function page() {
  const { user, accessRights, isLoading, isAuthenticated } = useAuth();

  useEffect(() => {
    console.log("User:", user);
    console.log("Access Rights:", accessRights);
    console.log("Is Authenticated:", isAuthenticated);
  }, [user, accessRights, isAuthenticated]);

  // Loading-Zustand während der Authentifizierungsprüfung
  if (isLoading) {
    return (
      <Box sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh'
      }}>
        <Typography variant="h6">Laden...</Typography>
      </Box>
    );
  }

  // Nicht authentifiziert - wird automatisch von AuthContext zur Login-Seite umgeleitet
  if (!isAuthenticated || !user) {
    return (
      <Box sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh'
      }}>
        <Typography variant="h6">Umleitung zur Anmeldung...</Typography>
      </Box>
    );
  }
  
  return (
    <Box sx={{
      maxHeight: '100vh',
    }}>
      <Container maxWidth="lg">
        <Box sx={{
          textAlign: 'center',
          mb: 6,
          mt: 4
        }}>
          <Typography
            variant="h2"
            component="h1"
            gutterBottom
            sx={{
              fontWeight: 'bold',
              fontSize: { xs: '2.5rem', md: '3.5rem' },
              mb: 2
            }}
          >
            Management System
          </Typography>
          <Typography
            variant="h5"
            component="h2"
            sx={{
              fontSize: { xs: '1.1rem', md: '1.3rem' },
              fontWeight: 300,
              maxWidth: 600,
              mx: 'auto',
              lineHeight: 1.5
            }}
          >
            Wählen Sie eine Option, um mit der Verwaltung zu beginnen oder einen Checkout durchzuführen
          </Typography>
        </Box>

        <Box sx={{
          display: 'flex',
          flexDirection: 'row',
          justifyContent: 'center',
          alignItems: 'center',
          width: '100%',
          maxWidth: 1200,
          mx: 'auto',
          padding: 2,
          gap: 4,
          minHeight: '60vh'
        }}>
          {accessRights && (accessRights.includes("kasse") || accessRights?.includes("admin")) && <CheckoutCard />}
          <DashboardCard />
        </Box>
      </Container>
    </Box>
  );
};
