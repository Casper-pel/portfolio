import React, { useState } from 'react';
import {
    Box,
    Button,
    Typography,
    Modal,
    TextField,
    Paper,
    Alert,
    InputAdornment,
    IconButton
} from '@mui/material';
import {
    Visibility,
    VisibilityOff,
    Key as KeyIcon
} from '@mui/icons-material';
import { EmployeeService } from '../services/employeeService';
import { modalStyle } from '../model/modalStyle';

const UpdatePassword = () => {
    const [open, setOpen] = useState(false);
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showOldPassword, setShowOldPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const handleOpen = () => {
        setOpen(true);
        setError(null);
        setSuccess(null);
    };

    const handleClose = () => {
        setOpen(false);
        setOldPassword('');
        setNewPassword('');
        setConfirmPassword('');
        setError(null);
        setSuccess(null);
    };

    const validatePassword = (password: string): boolean => {
        // Mindestens 8 Zeichen, ein Großbuchstabe, ein Kleinbuchstabe, eine Zahl
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/;
        return passwordRegex.test(password);
    };

    const handleUpdatePassword = async () => {
        if (!oldPassword || !newPassword || !confirmPassword) {
            setError('Bitte füllen Sie alle Felder aus');
            return;
        }

        if (newPassword !== confirmPassword) {
            setError('Die neuen Passwörter stimmen nicht überein');
            return;
        }

        if (!validatePassword(newPassword)) {
            setError('Das neue Passwort muss mindestens 8 Zeichen lang sein und einen Großbuchstaben, einen Kleinbuchstaben und eine Zahl enthalten');
            return;
        }

        if (oldPassword === newPassword) {
            setError('Das neue Passwort darf nicht mit dem alten Passwort identisch sein');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            await EmployeeService.updatePassword(oldPassword, newPassword);
            setSuccess('Passwort erfolgreich aktualisiert');
            
            // Modal nach kurzer Verzögerung schließen
            setTimeout(() => {
                handleClose();
            }, 2000);
        } catch (error: any) {
            setError(error.message || 'Fehler beim Aktualisieren des Passworts');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <Button
                variant="contained"
                color="primary"
                startIcon={<KeyIcon />}
                onClick={handleOpen}
                sx={{ mb: 2 }}
            >
                Passwort aktualisieren
            </Button>

            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="update-password-modal-title"
                aria-describedby="update-password-modal-description"
            >
                <Paper sx={{
                    ...modalStyle,
                    maxHeight: '90vh',
                    overflowY: 'auto',
                    p: 3,
                    width: '500px'
                }}>
                    <Typography id="update-password-modal-title" variant="h6" component="h2" gutterBottom>
                        Passwort aktualisieren
                    </Typography>
                    
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                        Aus Sicherheitsgründen müssen Sie Ihr aktuelles Passwort eingeben, um es zu ändern.
                    </Typography>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    {success && (
                        <Alert severity="success" sx={{ mb: 2 }}>
                            {success}
                        </Alert>
                    )}

                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                        <TextField
                            required
                            fullWidth
                            label="Aktuelles Passwort"
                            type={showOldPassword ? 'text' : 'password'}
                            value={oldPassword}
                            onChange={(e) => setOldPassword(e.target.value)}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowOldPassword(!showOldPassword)}
                                            edge="end"
                                        >
                                            {showOldPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                )
                            }}
                        />

                        <TextField
                            required
                            fullWidth
                            label="Neues Passwort"
                            type={showNewPassword ? 'text' : 'password'}
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            helperText="Min. 8 Zeichen, ein Groß- und Kleinbuchstabe, eine Zahl"
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowNewPassword(!showNewPassword)}
                                            edge="end"
                                        >
                                            {showNewPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                )
                            }}
                        />

                        <TextField
                            required
                            fullWidth
                            label="Neues Passwort bestätigen"
                            type={showConfirmPassword ? 'text' : 'password'}
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                            edge="end"
                                        >
                                            {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                )
                            }}
                        />
                    </Box>

                    <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
                        <Button
                            variant="outlined"
                            onClick={handleClose}
                            disabled={loading}
                            sx={{ flex: 1 }}
                        >
                            Abbrechen
                        </Button>
                        <Button
                            variant="contained"
                            onClick={handleUpdatePassword}
                            disabled={loading || !oldPassword || !newPassword || !confirmPassword}
                            sx={{ flex: 1 }}
                        >
                            {loading ? 'Wird aktualisiert...' : 'Passwort aktualisieren'}
                        </Button>
                    </Box>
                </Paper>
            </Modal>
        </>
    );
};

export default UpdatePassword;