"use client";

import React, { useState, useEffect } from 'react';
import {
    Box,
    Button,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
    Chip,
    IconButton,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Alert,
    Tooltip
} from '@mui/material';
import {
    Visibility as VisibilityIcon,
    Edit as EditIcon,
    Delete as DeleteIcon
} from '@mui/icons-material';
import dayjs from 'dayjs';
import 'dayjs/locale/de';
import EditAntragModal from './EditAntragModal';
import { UrlaubsAntragDto } from '../model/urlaubsAntragDto';
import { Employee, EmployeeService } from '../services/employeeService';
import { UrlaubsAntragService } from '../services/urlaubsAntragService';

const MeineAntraege: React.FC = () => {
    const [antraege, setAntraege] = useState<UrlaubsAntragDto[]>([]);
    const [selectedAntrag, setSelectedAntrag] = useState<UrlaubsAntragDto | null>(null);
    const [detailsOpen, setDetailsOpen] = useState(false);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [editModalOpen, setEditModalOpen] = useState(false);
    const [antragToDelete, setAntragToDelete] = useState<UrlaubsAntragDto | null>(null);
    const [antragToEdit, setAntragToEdit] = useState<UrlaubsAntragDto | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [employees, setEmployees] = useState<Map<number, Employee>>(new Map());

    useEffect(() => {
        fetchMeineAntraege();
        fetchEmployees();
    }, []);

    const fetchEmployees = async () => {
        try {
            const allEmployees = await EmployeeService.getAllEmployees();
            const employeeMap = new Map<number, Employee>();
            allEmployees.forEach(employee => {
                employeeMap.set(employee.employeeId, employee);
            });
            setEmployees(employeeMap);
        } catch (err) {
            console.error('Error fetching employees:', err);
        }
    };

    const fetchMeineAntraege = async () => {
        setLoading(true);
        try {
            const data = await UrlaubsAntragService.getByEmployeeId();
            setAntraege(data);
        } catch (err) {
            setError('Fehler beim Laden Ihrer Anträge');
        } finally {
            setLoading(false);
        }
    };

    const handleViewDetails = (antrag: UrlaubsAntragDto) => {
        setSelectedAntrag(antrag);
        setDetailsOpen(true);
    };

    const handleCloseDetails = () => {
        setDetailsOpen(false);
        setSelectedAntrag(null);
        setError(null);
        setSuccess(null);
    };

    const handleEditAntrag = (antrag: UrlaubsAntragDto) => {
        setAntragToEdit(antrag);
        setEditModalOpen(true);
    };

    const handleSaveEditedAntrag = async (antragData: UrlaubsAntragDto) => {
        try {
            if (antragData.antragsId) {
                await UrlaubsAntragService.update(antragData);
                setSuccess('Antrag wurde erfolgreich aktualisiert');
            } else {
                await UrlaubsAntragService.create(antragData);
                setSuccess('Antrag wurde erfolgreich erstellt');
            }
            fetchMeineAntraege();
            setEditModalOpen(false);
        } catch (err) {
            throw new Error('Fehler beim ' + (antragData.antragsId ? 'Aktualisieren' : 'Erstellen') + ' des Antrags');
        }
    };

    const handleDeleteRequest = (antrag: UrlaubsAntragDto) => {
        setAntragToDelete(antrag);
        setDeleteDialogOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!antragToDelete) return;

        setLoading(true);
        setError(null);

        try {
            await UrlaubsAntragService.delete(antragToDelete.antragsId!);
            fetchMeineAntraege();
            setSuccess('Antrag wurde erfolgreich gelöscht');
            setDeleteDialogOpen(false);
        } catch (err) {
            setError('Fehler beim Löschen des Antrags');
        } finally {
            setLoading(false);
        }
    };

    const getStatusChip = (status: string) => {
        switch (status) {
            case 'pending':
                return <Chip label="Ausstehend" color="warning" size="small" />;
            case 'genehmigt':
                return <Chip label="Genehmigt" color="success" size="small" />;
            case 'abgelehnt':
                return <Chip label="Abgelehnt" color="error" size="small" />;
            default:
                return <Chip label={status} size="small" />;
        }
    };

    const calculateDays = (startDate: string, endDate: string) => {
        const start = dayjs(startDate);
        const end = dayjs(endDate);
        return end.diff(start, 'day') + 1;
    };

    const formatDate = (dateString: string) => {
        return dayjs(dateString).format('DD.MM.YYYY');
    };

    const formatDateTime = (dateString: string) => {
        return dayjs(dateString).format('DD.MM.YYYY HH:mm');
    };

    const canEditOrDelete = (antrag: UrlaubsAntragDto) => {
        return antrag.status === 'pending';
    };

    const getEmployeeName = (employeeId: number | null): string => {
        if (!employeeId) return 'Unbekannt';
        const employee = employees.get(employeeId);
        return employee ? EmployeeService.formatEmployeeName(employee) : `Mitarbeiter ${employeeId}`;
    };

    return (
        <Box>
            <Typography variant="h5" gutterBottom>
                Meine Urlaubsanträge
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

            {antraege.length === 0 ? (
                <Paper sx={{ p: 4, textAlign: 'center' }}>
                    <Typography variant="h6" color="text.secondary">
                        Sie haben noch keine Urlaubsanträge eingereicht
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        Wechseln Sie zum Tab &#34;Neuen Antrag stellen&#34;, um Ihren ersten Antrag einzureichen.
                    </Typography>
                </Paper>
            ) : (
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Zeitraum</TableCell>
                                <TableCell>Tage</TableCell>
                                <TableCell>Art</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell>Erstellt von</TableCell>
                                <TableCell>Eingereicht am</TableCell>
                                <TableCell align="center">Aktionen</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {antraege.map((antrag) => (
                                <TableRow key={antrag.antragsId} hover>
                                    <TableCell>
                                        {formatDate(antrag.startDatum)} - {formatDate(antrag.endDatum)}
                                    </TableCell>
                                    <TableCell>
                                        {calculateDays(antrag.startDatum, antrag.endDatum)}
                                    </TableCell>
                                    <TableCell>{antrag.type}</TableCell>
                                    <TableCell>{getStatusChip(antrag.status)}</TableCell>
                                    <TableCell>{getEmployeeName(antrag.employeeId)}</TableCell>
                                    <TableCell>{antrag.reviewDate ? formatDate(antrag.reviewDate) : '-'}</TableCell>
                                    <TableCell align="center">
                                        <Tooltip title="Details anzeigen">
                                            <IconButton
                                                onClick={() => handleViewDetails(antrag)}
                                                size="small"
                                            >
                                                <VisibilityIcon />
                                            </IconButton>
                                        </Tooltip>
                                        {canEditOrDelete(antrag) && (
                                            <>
                                                <Tooltip title="Bearbeiten">
                                                    <IconButton
                                                        onClick={() => handleEditAntrag(antrag)}
                                                        size="small"
                                                        color="primary"
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
                                                </Tooltip>
                                            <Tooltip title="Löschen">
                                                <IconButton
                                                    onClick={() => handleDeleteRequest(antrag)}
                                                    size="small"
                                                    color="error"
                                                >
                                                    <DeleteIcon />
                                                </IconButton>
                                            </Tooltip>
                                            </>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}

            {/* Details Dialog */}
            <Dialog
                open={detailsOpen}
                onClose={handleCloseDetails}
                maxWidth="md"
                fullWidth
            >
                <DialogTitle>
                    Mein Urlaubsantrag - Details
                </DialogTitle>
                <DialogContent>
                    {selectedAntrag && (
                        <Box sx={{ mt: 2 }}>
                            <Typography variant="h6" gutterBottom>
                                Antragsdetails
                            </Typography>

                            <Box sx={{ mb: 2 }}>
                                <Typography><strong>Zeitraum:</strong> {formatDate(selectedAntrag.startDatum)} - {formatDate(selectedAntrag.endDatum)}</Typography>
                                <Typography><strong>Anzahl Tage:</strong> {calculateDays(selectedAntrag.startDatum, selectedAntrag.endDatum)}</Typography>
                                <Typography><strong>Art:</strong> {selectedAntrag.type}</Typography>
                                <Typography><strong>Grund:</strong> {selectedAntrag.grund}</Typography>
                                <Typography><strong>Status:</strong> {getStatusChip(selectedAntrag.status)}</Typography>
                                <Typography><strong>Erstellt von:</strong> {getEmployeeName(selectedAntrag.employeeId)}</Typography>
                                <Typography><strong>Bewertungsdatum:</strong> {selectedAntrag.reviewDate ? formatDate(selectedAntrag.reviewDate) : '-'}</Typography>
                            </Box>

                            {selectedAntrag.comment && (
                                <Box sx={{ mb: 2 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Reviewer Kommentar
                                    </Typography>
                                    <Typography>{selectedAntrag.comment}</Typography>
                                </Box>
                            )}

                            {selectedAntrag.status !== 'pending' && (
                                <Box sx={{ mb: 2 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Bewertung
                                    </Typography>
                                    <Typography><strong>Bearbeitet von:</strong> {getEmployeeName(selectedAntrag.reviewerId ?? null)}</Typography>
                                    <Typography><strong>Bewertungsdatum:</strong> {selectedAntrag.reviewDate ? formatDateTime(selectedAntrag.reviewDate) : '-'}</Typography>
                                    <Typography><strong>Status:</strong> {selectedAntrag.status === 'genehmigt' ? 'Genehmigt' : 'Abgelehnt'}</Typography>
                                </Box>
                            )}
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDetails}>
                        Schließen
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Delete Confirmation Dialog */}
            <Dialog
                open={deleteDialogOpen}
                onClose={() => setDeleteDialogOpen(false)}
            >
                <DialogTitle>
                    Antrag löschen
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        Möchten Sie diesen Urlaubsantrag wirklich löschen?
                    </Typography>
                    {antragToDelete && (
                        <Box sx={{ mt: 2, p: 2, backgroundColor: 'background.default', borderRadius: 1 }}>
                            <Typography variant="body2">
                                <strong>Zeitraum:</strong> {formatDate(antragToDelete.startDatum)} - {formatDate(antragToDelete.endDatum)}
                            </Typography>
                            <Typography variant="body2">
                                <strong>Art:</strong> {antragToDelete.type}
                            </Typography>
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteDialogOpen(false)} disabled={loading}>
                        Abbrechen
                    </Button>
                    <Button
                        onClick={handleConfirmDelete}
                        color="error"
                        variant="contained"
                        disabled={loading}
                    >
                        {loading ? 'Wird gelöscht...' : 'Löschen'}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Edit Antrag Modal */}
            <EditAntragModal
                open={editModalOpen}
                onClose={() => {
                    setEditModalOpen(false);
                    setAntragToEdit(null);
                }}
                onSave={handleSaveEditedAntrag}
                antrag={antragToEdit}
            />
        </Box>
    );
};

export default MeineAntraege;
