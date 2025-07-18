'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { 
  Box, 
  Container, 
  Paper, 
  Typography, 
  Button, 
  Fade,
  CircularProgress,
  Stack
} from '@mui/material';
import { CheckCircle } from '@mui/icons-material';
import { blue } from '@mui/material/colors';

export default function CheckoutComplete() {
  const router = useRouter();
  
  const [countdown, setCountdown] = useState(5);
  const [fadeIn, setFadeIn] = useState(false);

  useEffect(() => {
    setFadeIn(true);
    
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          router.push('/kassa');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [router]);

  const handleRedirectNow = () => {
    router.push('/kassa');
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          py: 3
        }}
      >
        <Fade in={fadeIn} timeout={800}>
          <Paper
            elevation={8}
            sx={{
              p: 6,
              textAlign: 'center',
              borderRadius: 4,
              background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)',
              border: `2px solid ${blue[100]}`,
            }}
          >
            <Stack spacing={3} alignItems="center">
              <CheckCircle 
                sx={{ 
                  fontSize: 80, 
                  color: blue[500],
                  animation: 'pulse 2s infinite'
                }} 
              />
              
              <Typography 
                variant="h3" 
                component="h1" 
                sx={{ 
                  fontWeight: 'bold',
                  color: blue[600],
                  mb: 2
                }}
              >
                Zahlung erfolgreich!
              </Typography>
              
              <Typography 
                variant="h6" 
                color="text.secondary"
                sx={{ maxWidth: 400, lineHeight: 1.6 }}
              >
                Vielen Dank für deinen Einkauf. Deine Zahlung wurde erfolgreich abgeschlossen.
              </Typography>

              <Box sx={{ mt: 4, mb: 2 }}>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Automatische Weiterleitung in {countdown} Sekunden...
                </Typography>
                <CircularProgress 
                  variant="determinate" 
                  value={(6 - countdown) * 20} 
                  size={40}
                  sx={{ color: blue[500] }}
                />
              </Box>

              <Button
                variant="contained"
                size="large"
                onClick={handleRedirectNow}
                sx={{
                  px: 4,
                  py: 1.5,
                  fontSize: '1.1rem',
                  borderRadius: 3,
                  background: `linear-gradient(45deg, ${blue[500]} 30%, ${blue[600]} 90%)`,
                  boxShadow: '0 3px 15px rgba(33, 150, 243, 0.3)',
                  '&:hover': {
                    background: `linear-gradient(45deg, ${blue[600]} 30%, ${blue[700]} 90%)`,
                    boxShadow: '0 5px 20px rgba(33, 150, 243, 0.4)',
                    transform: 'translateY(-2px)',
                  },
                  transition: 'all 0.3s ease'
                }}
              >
                Jetzt zur Kassa
              </Button>
            </Stack>
          </Paper>
        </Fade>
      </Box>

      <style jsx>{`
        @keyframes pulse {
          0% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.1);
          }
          100% {
            transform: scale(1);
          }
        }
      `}</style>
    </Container>
  );
}