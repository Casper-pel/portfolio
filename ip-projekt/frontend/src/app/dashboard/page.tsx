"use client";

import { useEffect, useState } from 'react';
import { useAuth, User } from '../context/AuthContext';
import { Box, Typography } from '@mui/material';
import UpdatePassword from '../components/Employees/UpdatePassword';

const page = () => {

  const { user, isLoading, isAuthenticated } = useAuth();

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
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '80vh',
      flexDirection: 'column',
      gap: '20px'
    }}>
      <h1 style={{
        fontSize: '2.5rem',
        margin: 0,
        color: '#333'
      }}>
        Willkommen, {user?.firstName} {user?.lastName}!
      </h1>
      <p style={{
        fontSize: '1.2rem',
        color: '#666',
        margin: 0
      }}>
        Schön, Sie wieder zu sehen.
      </p>
      <div>
        <UpdatePassword />
      </div>
    </div>
  );
};

export default page;