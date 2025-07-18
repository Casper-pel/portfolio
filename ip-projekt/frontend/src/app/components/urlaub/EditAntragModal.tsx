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
import { Employee, EmployeeService } from '../services/employeeService';
import { modalStyle } from '../model/modalStyle';

interface EditAntragModalProps {
    open: boolean;
    onClose: () => void;
    onSave: (antragData: UrlaubsAntragDto) => Promise<void>;
    antrag?: UrlaubsAntragDto | null;
}

const EditAntragModal: React.FC<EditAntragModalProps> = ({
    open,
    onClose,
    onSave,
    antrag
}) => {
    const [employeeId, setEmployeeId] = React.useState<number | null>(null);
    const [startDate, setStartDate] = React.useState<Dayjs | null>(null);
    const [endDate, setEndDate] = React.useState<Dayjs | null>(null);
    const [art, setArt] = React.useState('');
    const [grund, setGrund] = React.useState('');
    const [comment, setComment] = React.useState('');
    const [error, setError] = React.useState<string | null>(null);
    const [success, setSuccess] = React.useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = React.useState(false);
    const [creatorEmployee, setCreatorEmployee] = React.useState<Employee | null>(null);
    const [reviewerEmployee, setReviewerEmployee] = React.useState<Employee | null>(null);

    React.useEffect(() => {
        const loadEmployeeData = async () => {
            if (antrag && open) {
                setEmployeeId(antrag.employeeId);
                setStartDate(dayjs(antrag.startDatum));
                setEndDate(dayjs(antrag.endDatum));
                setArt(antrag.type);
                setGrund(antrag.grund);
                setComment(antrag.comment || '');

                try {
                    const creator = await EmployeeService.getEmployeeById(antrag.employeeId);
                    setCreatorEmployee(creator);
                } catch (err) {
                    console.error('Error loading creator employee data:', err);
                }

                if (antrag.reviewerId && antrag.status !== 'pending') {
                    try {
                        const reviewer = await EmployeeService.getEmployeeById(antrag.reviewerId);
                        setReviewerEmployee(reviewer);
                    } catch (err) {
                        console.error('Error loading reviewer employee data:', err);
                    }
                }
            } else if (open) {
                setEmployeeId(null);
                setStartDate(null);
                setEndDate(null);
                setArt('');
                setGrund('');
                setComment('');
                setCreatorEmployee(null);
                setReviewerEmployee(null);
            }
        };

        loadEmployeeData();
        setError(null);
        setSuccess(null);
    }, [antrag, open]);

    const handleClose = () => {
        onClose();
        // Alle Felder zurücksetzen
        setEmployeeId(null);
        setStartDate(null);
        setEndDate(null);
        setArt('');
        setGrund('');
        setComment('');
        setError(null);
        setSuccess(null);
        setIsSubmitting(false);
        setCreatorEmployee(null);
        setReviewerEmployee(null);
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

        if (antrag && !employeeId) {
            setError('Fehler: Mitarbeiter-ID nicht gefunden. Bitte laden Sie die Seite neu.');
            return;
        }

        setIsSubmitting(true);

        try {
            const antragData: UrlaubsAntragDto = {
                employeeId: employeeId ?? 0,
                startDatum: startDate!.format('YYYY-MM-DD'),
                endDatum: endDate!.format('YYYY-MM-DD'),
                type: art,
                grund: grund.trim(),
                status: antrag?.status || 'pending',
                reviewDate: antrag?.reviewDate || dayjs().add(1, 'day').format('YYYY-MM-DD'),
                reviewerId: antrag?.reviewerId || 1,
                comment: comment.trim() || undefined
            };

            if (antrag?.antragsId) {
                antragData.antragsId = antrag.antragsId;
            }

            await onSave(antragData);
            
            setSuccess(`Ihr Urlaubsantrag wurde erfolgreich ${antrag ? 'aktualisiert' : 'eingereicht'}!`);
            
            // Modal nach 1.5 Sekunden schließen
            setTimeout(() => {
                handleClose();
            }, 1500);

        } catch (err) {
            setError(`Fehler beim ${antrag ? 'Aktualisieren' : 'Einreichen'} des Urlaubsantrags. Bitte versuchen Sie es erneut.`);
            console.error('Error saving vacation request:', err);
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
        <Modal
            open={open}
            onClose={handleClose}
            aria-labelledby="edit-antrag-modal-title"
            aria-describedby="edit-antrag-modal-description"
        >
            <Paper sx={{
                ...modalStyle,
                maxHeight: '90vh',
                overflowY: 'auto',
                p: 3,
                width: '600px'
            }}>
                <Typography id="edit-antrag-modal-title" variant="h6" component="h2">
                    {antrag 
                        ? `Urlaubsantrag bearbeiten${creatorEmployee ? ` - ${EmployeeService.formatEmployeeName(creatorEmployee)}` : ` (ID: ${employeeId})`}` 
                        : 'Neuen Urlaubsantrag stellen'
                    }
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
                                onChange={(newValue) => setStartDate(dayjs(newValue))}
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
                            <MenuItem value="Sonderurlaub">Sonderurlaub</MenuItem>
                            <MenuItem value="Krankheit">Krankheit</MenuItem>
                            <MenuItem value="Bildungsurlaub">Bildungsurlaub</MenuItem>
                            <MenuItem value="Mutterschutz">Mutterschutz</MenuItem>
                            <MenuItem value="Elternzeit">Elternzeit</MenuItem>
                            <MenuItem value="Unbezahlter Urlaub">Unbezahlter Urlaub</MenuItem>
                            <MenuItem value="Pflegezeit">Pflegezeit</MenuItem>
                            <MenuItem value="Hochzeit">Hochzeit</MenuItem>
                            <MenuItem value="Beerdigung">Beerdigung</MenuItem>
                            <MenuItem value="Jubiläum">Jubiläum</MenuItem>
                            <MenuItem value="Dienstreise">Dienstreise</MenuItem>
                            <MenuItem value="Homeoffice">Homeoffice</MenuItem>
                            <MenuItem value="Sonstiges">Sonstiges</MenuItem>
                        </Select>
                    </FormControl>

                    <TextField
                        fullWidth
                        required
                        label="Grund"
                        className='grund-input'
                        placeholder="Geben Sie den Grund für Ihren Urlaubsantrag an..."
                        value={grund}
                        onChange={(e) => setGrund(e.target.value)}
                        sx={{ mt: 2 }}
                        error={!grund.trim() && error !== null}
                        helperText={!grund.trim() && error !== null ? "Grund ist erforderlich" : ""}
                    />

                    {/* Show review information if request has been reviewed */}
                    {antrag && antrag.status !== 'pending' && (
                        <Box sx={{ mt: 3, p: 2, backgroundColor: 'background.default', borderRadius: 1 }}>
                            <Typography variant="h6" gutterBottom color="primary">
                                Bewertungsinformationen
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Status:</strong> {antrag.status === 'genehmigt' ? '✅ Genehmigt' : '❌ Abgelehnt'}
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Bearbeitet von:</strong> {reviewerEmployee 
                                    ? EmployeeService.formatEmployeeName(reviewerEmployee)
                                    : `Reviewer ${antrag.reviewerId}`
                                }
                            </Typography>
                            <Typography variant="body2" sx={{ mb: 1 }}>
                                <strong>Bewertungsdatum:</strong> {antrag.reviewDate 
                                    ? dayjs(antrag.reviewDate).format('DD.MM.YYYY HH:mm')
                                    : '-'
                                }
                            </Typography>
                            {antrag.comment && (
                                <Typography variant="body2" sx={{ mt: 1 }}>
                                    <strong>Kommentar:</strong> {antrag.comment}
                                </Typography>
                            )}
                        </Box>
                    )}

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
                            {isSubmitting 
                                ? (antrag ? 'Wird aktualisiert...' : 'Wird eingereicht...') 
                                : (antrag ? 'Aktualisieren' : 'Antrag einreichen')
                            }
                        </Button>
                    </Box>
                </Box>
            </Paper>
        </Modal>
    );
};

export default EditAntragModal;
