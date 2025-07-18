"use client";

import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import { FormControl, InputLabel, MenuItem, Paper, Select, TextField, Alert } from '@mui/material';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { DemoContainer } from '@mui/x-date-pickers/internals/demo';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/de';
import { UrlaubsAntragDto } from '../model/urlaubsAntragDto';
import { UrlaubsAntragService } from '../services/urlaubsAntragService';
import { modalStyle } from '../model/modalStyle';

export default function UrlaubsantragModal() {
    const [open, setOpen] = React.useState(false);
    const [startDate, setStartDate] = React.useState<Dayjs | null>(null);
    const [endDate, setEndDate] = React.useState<Dayjs | null>(null);
    const [art, setArt] = React.useState('');
    const [grund, setGrund] = React.useState('');
    const [comment, setComment] = React.useState('');
    const [error, setError] = React.useState<string | null>(null);
    const [success, setSuccess] = React.useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = React.useState(false);

    const handleOpen = () => {
        setOpen(true);
        setError(null);
        setSuccess(null);
    };

    const handleClose = () => {
        setOpen(false);
        // Reset form
        setStartDate(null);
        setEndDate(null);
        setArt('');
        setGrund('');
        setComment('');
        setError(null);
        setSuccess(null);
        setIsSubmitting(false);
    };

    const validateForm = () => {
        if (!startDate) {
            setError('Bitte wählen Sie ein Startdatum');
            return false;
        }
        if (!endDate) {
            setError('Bitte wählen Sie ein Enddatum');
            return false;
        }
        if (!art) {
            setError('Bitte wählen Sie eine Art des Urlaubsantrags');
            return false;
        }
        if (!grund.trim()) {
            setError('Bitte geben Sie einen Grund für den Urlaubsantrag an');
            return false;
        }
        if (endDate.isBefore(startDate)) {
            setError('Das Enddatum muss nach dem Startdatum liegen');
            return false;
        }
        if (startDate.isBefore(dayjs(), 'day')) {
            setError('Das Startdatum darf nicht in der Vergangenheit liegen');
            return false;
        }
        return true;
    };

    const handleSubmit = async () => {
        setError(null);
        setSuccess(null);

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);

        try {
            const requestData: UrlaubsAntragDto = {
                employeeId: 0, // Wird vom Backend überschrieben mit der echten ID aus dem Token
                startDatum: startDate!.format('YYYY-MM-DD'),
                endDatum: endDate!.format('YYYY-MM-DD'),
                type: art,
                grund: grund.trim(),
                comment: comment.trim(),
                status: 'pending' as const
                // reviewDate und reviewerId werden weggelassen da optional und erst bei Review gesetzt werden
            };

            console.log('Sending Urlaubsantrag request:', requestData);
            await UrlaubsAntragService.create(requestData);

            setSuccess('Ihr Urlaubsantrag wurde erfolgreich eingereicht!');
            setTimeout(() => handleClose(), 2000);
        } catch (err) {
            console.error('Fehler beim Einreichen des Antrags:', err);
            setError('Fehler beim Einreichen des Antrags. Bitte versuchen Sie es erneut.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const calculateDays = () => {
        if (startDate && endDate && endDate.isAfter(startDate)) {
            return endDate.diff(startDate, 'day') + 1;
        }
        return 0;
    };
    return (
        <div>
            <Button
                variant="contained"
                sx={{
                    mt: 2,
                }}
                onClick={handleOpen}
            >Urlaubsantrag</Button>
            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Paper sx={{
                    ...modalStyle,
                    maxHeight: '90vh',
                    overflowY: 'auto',
                    p: 3,
                    width: '600px'
                }}>
                    <Typography id="modal-modal-title" variant="h6" component="h2">
                        Stelle deinen Urlaubsantrag
                    </Typography>

                    {error && (
                        <Alert severity="error" sx={{ mt: 2 }}>
                            {error}
                        </Alert>
                    )}

                    {success && (
                        <Alert severity="success" sx={{ mt: 2 }}>
                            {success}
                        </Alert>
                    )}

                    <Box sx={{ mt: 2 }} >
                        <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="de">
                            <DemoContainer components={['DatePicker', 'DatePicker']}>
                                <DatePicker
                                    label="Startdatum wählen"
                                    className='start-date-picker'
                                    value={startDate}
                                    onChange={(value) => setStartDate(dayjs(value))}
                                    slotProps={{
                                        textField: {
                                            required: true,
                                            error: !startDate && error !== null
                                        }
                                    }}
                                />
                                <DatePicker
                                    label="Enddatum wählen"
                                    className='end-date-picker'
                                    value={endDate}
                                    onChange={(newValue) => setEndDate(dayjs(newValue))}
                                    slotProps={{
                                        textField: {
                                            required: true,
                                            error: !endDate && error !== null
                                        }
                                    }}
                                />
                            </DemoContainer>
                        </LocalizationProvider>

                        {startDate && endDate && calculateDays() > 0 && (
                            <Typography variant="body2" sx={{ mt: 1, color: 'text.secondary' }}>
                                Anzahl Tage: {calculateDays()}
                            </Typography>
                        )}

                        <FormControl fullWidth sx={{ mt: 2 }} required>
                            <InputLabel id="art-select-label">Art des Urlaubsantrags</InputLabel>
                            <Select
                                labelId="art-select-label"
                                id="art-select"
                                value={art}
                                label="Art des Urlaubsantrags"
                                onChange={(e) => setArt(e.target.value as string)}
                                error={!art && error !== null}
                            >
                                <MenuItem value="Erholungsurlaub">Erholungsurlaub</MenuItem>
                                <MenuItem value="Krankheit">Krankheit (mit ärztlichem Attest)</MenuItem>
                                <MenuItem value="Bildungsurlaub">Bildungsurlaub</MenuItem>
                                <MenuItem value="Mutterschutz">Mutterschutz</MenuItem>
                                <MenuItem value="Elternzeit">Elternzeit</MenuItem>
                                <MenuItem value="Pflegezeit">Pflegezeit</MenuItem>
                                <MenuItem value="Unbezahlter Urlaub">Unbezahlter Urlaub</MenuItem>
                                <MenuItem value="Sonderurlaub">Sonderurlaub (z. B. Hochzeit, Beerdigung, Jubiläum)</MenuItem>
                                <MenuItem value="Sonstiges">Sonstiges</MenuItem>
                            </Select>
                        </FormControl>

                        <TextField
                            fullWidth
                            label="Grund für den Urlaubsantrag"
                            placeholder="Grund für Ihren Urlaubsantrag..."
                            value={grund}
                            onChange={(e) => setGrund(e.target.value)}
                            sx={{ mt: 2 }}
                            required
                            error={!grund.trim() && error !== null}
                            inputProps={{ maxLength: 255 }}
                            helperText={`${grund.length}/255 Zeichen`}
                        />

                        <Box sx={{ mt: 3, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                            <Button
                                variant="outlined"
                                onClick={handleClose}
                                disabled={isSubmitting}
                            >
                                Abbrechen
                            </Button>
                            <Button
                                variant="contained"
                                onClick={handleSubmit}
                                disabled={isSubmitting || !startDate || !endDate || !art || !grund.trim()}
                            >
                                {isSubmitting ? 'Wird eingereicht...' : 'Antrag einreichen'}
                            </Button>
                        </Box>
                    </Box>
                </Paper>
            </Modal>
        </div>
    );
}
