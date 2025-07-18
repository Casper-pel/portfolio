"use client";

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useRouter } from 'next/navigation';
import DashboardOutlined from '@mui/icons-material/DashboardOutlined';

export default function DashboardCard() {
    const router = useRouter();

    const handleNavigate = () => {
        router.push('/dashboard');
    };

    return (
        <Card
            sx={{
                minWidth: 300,
                maxWidth: 400,
                height: 300,
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                cursor: 'pointer',
                transition: 'all 0.3s ease',
                '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: '0 8px 25px rgba(0,0,0,0.15)',
                }
            }}
            onClick={handleNavigate}
        >
            <CardContent sx={{ textAlign: 'center', flexGrow: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                <Box sx={{ mb: 2 }}>
                    <DashboardOutlined sx={{ fontSize: 60, color: 'primary.main' }} />
                </Box>
                <Typography variant="h5" component="div" gutterBottom sx={{ fontWeight: 'bold' }}>
                    Dashboard
                </Typography>
                <Typography sx={{ color: 'text.secondary', mb: 2 }}>
                    Mitarbeiter und Rollen verwalten
                </Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                    Übersicht über Mitarbeiter, Rollen und Berechtigungen im System
                </Typography>
            </CardContent>
            <CardActions sx={{ justifyContent: 'center', pb: 3 }}>
                <Button size="large" variant="contained" color="primary" sx={{ minWidth: 140 }} onClick={handleNavigate}>
                    Zum Dashboard
                </Button>
            </CardActions>
        </Card>
    );
}
