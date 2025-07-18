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
    TextField,
    Alert,
    Tooltip,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Card,
    CardContent,
    Stack,
    Collapse
} from '@mui/material';
import {
    Visibility as VisibilityIcon,
    BuildSharp,
    FilterList as FilterListIcon,
    Clear as ClearIcon,
    ExpandMore as ExpandMoreIcon,
    ExpandLess as ExpandLessIcon
} from '@mui/icons-material';
import dayjs from 'dayjs';
import 'dayjs/locale/de';
import { Employee, EmployeeService } from '../services/employeeService';
import { UrlaubsAntragService } from '../services/urlaubsAntragService';
import { useAuth } from '@/app/context/AuthContext';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';

interface UrlaubsantragData {
    antragsId?: number;
    employeeId: number;
    startDatum: string;
    endDatum: string;
    art: string;
    comment: string;
    status: 'pending' | 'genehmigt' | 'abgelehnt';
    type: string;
    submittedAt: string;
    reviewDate?: string;
    reviewerId?: number;
    reviewComment?: string;
}

const AntragTabelle: React.FC = () => {
    const { user, accessRights } = useAuth();

    console.log("Access Rights in table:", accessRights);
    const [antraege, setAntraege] = useState<UrlaubsantragData[]>([]);
    const [filteredAntraege, setFilteredAntraege] = useState<UrlaubsantragData[]>([]);
    const [selectedAntrag, setSelectedAntrag] = useState<UrlaubsantragData | null>(null);
    const [detailsOpen, setDetailsOpen] = useState(false);
    const [reviewComment, setReviewComment] = useState('');
    const [filter, setFilter] = useState(true); // Toggle for filter section visibility - default open
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [employees, setEmployees] = useState<Map<number, Employee>>(new Map());
    const [showFilters, setShowFilters] = useState<boolean>(false);

    // Filter States
    const [statusFilter, setStatusFilter] = useState<string>('alle');
    const [employeeFilter, setEmployeeFilter] = useState<string>('alle');
    const [dateFilter, setDateFilter] = useState<dayjs.Dayjs | null>(null);
    const [sortBy, setSortBy] = useState<string>('submittedAt');
    const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

    useEffect(() => {
        fetchAntraege();
        fetchEmployees();
    }, []);

    useEffect(() => {
        applyFiltersAndSort();
    }, [antraege, statusFilter, employeeFilter, dateFilter, sortBy, sortOrder]);

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

    const fetchAntraege = async () => {
        setLoading(true);
        try {
            const data = await UrlaubsAntragService.getAll();
            setAntraege(data);
        } catch (err) {
            setError('Fehler beim Laden der Anträge');
        } finally {
            setLoading(false);
        }
    };

    const applyFiltersAndSort = () => {
        let filtered = [...antraege];

        // Status Filter
        if (statusFilter !== 'alle') {
            filtered = filtered.filter(antrag => antrag.status === statusFilter);
        }

        // Employee Filter
        if (employeeFilter !== 'alle') {
            filtered = filtered.filter(antrag => antrag.employeeId.toString() === employeeFilter);
        }

        // Date Filter - zeigt Anträge die an dem gewählten Datum starten, enden oder dazwischen liegen
        if (dateFilter) {
            filtered = filtered.filter(antrag => {
                const antragStart = dayjs(antrag.startDatum);
                const antragEnd = dayjs(antrag.endDatum);
                const filterDate = dateFilter;
                
                // Antrag ist relevant wenn das gewählte Datum zwischen Start- und Enddatum liegt (inklusive)
                return (filterDate.isSame(antragStart, 'day') || 
                        filterDate.isSame(antragEnd, 'day') || 
                        (filterDate.isAfter(antragStart, 'day') && filterDate.isBefore(antragEnd, 'day')));
            });
        }

        // Sorting
        filtered.sort((a, b) => {
            let aValue: any, bValue: any;

            switch (sortBy) {
                case 'employee':
                    aValue = getEmployeeName(a.employeeId);
                    bValue = getEmployeeName(b.employeeId);
                    break;
                case 'startDatum':
                    aValue = dayjs(a.startDatum);
                    bValue = dayjs(b.startDatum);
                    break;
                case 'status':
                    aValue = a.status;
                    bValue = b.status;
                    break;
                case 'submittedAt':
                default:
                    aValue = dayjs(a.submittedAt);
                    bValue = dayjs(b.submittedAt);
                    break;
            }

            if (sortBy === 'startDatum' || sortBy === 'submittedAt') {
                return sortOrder === 'asc' ? aValue.diff(bValue) : bValue.diff(aValue);
            } else {
                const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
                return sortOrder === 'asc' ? comparison : -comparison;
            }
        });

        setFilteredAntraege(filtered);
    };

    const clearFilters = () => {
        setStatusFilter('alle');
        setEmployeeFilter('alle');
        setDateFilter(null);
        setSortBy('submittedAt');
        setSortOrder('desc');
    };

    const handleViewDetails = (antrag: UrlaubsantragData) => {
        setSelectedAntrag(antrag);
        setReviewComment(antrag.reviewComment || '');
        setDetailsOpen(true);
    };

    const handleOpenReviewDialog = (antrag: UrlaubsantragData) => {
        if (!antrag.antragsId) {
            setError('Dieser Antrag kann noch nicht bearbeitet werden - ID fehlt');
            return;
        }
        setSelectedAntrag(antrag);
        setReviewComment('');
        setDetailsOpen(true);
    };

    const handleCloseDetails = () => {
        setDetailsOpen(false);
        setSelectedAntrag(null);
        setReviewComment('');
        setError(null);
        setSuccess(null);
    };

    const handleReviewAntrag = async (status: 'APPROVED' | 'REJECTED') => {
        if (!selectedAntrag) return;

        if (!selectedAntrag.antragsId || selectedAntrag.antragsId === null || selectedAntrag.antragsId === undefined) {
            setError('Antrag ID ist ungültig - kann nicht bearbeitet werden');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const backendStatus = status === 'APPROVED' ? 'genehmigt' : 'abgelehnt';

            await UrlaubsAntragService.review(
                selectedAntrag.antragsId,
                backendStatus,
                reviewComment,
                user?.employeeId || 1
            );

            fetchAntraege();
            setSuccess(`Antrag wurde ${status === 'APPROVED' ? 'genehmigt' : 'abgelehnt'}`);

            setTimeout(() => {
                handleCloseDetails();
            }, 1500);
        } catch (err) {
            setError('Fehler beim Bearbeiten des Antrags');
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

    const getEmployeeName = (employeeId: number | null): string => {
        if (!employeeId) return 'Unbekannt';
        const employee = employees.get(employeeId);
        return employee ? EmployeeService.formatEmployeeName(employee) : `Mitarbeiter ${employeeId}`;
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="de">
            <Box sx={{ p: 3 }}>
                <Typography variant="h5" gutterBottom>
                    Urlaubsanträge
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

            {/* Filter Section */}
            <Card sx={{ mb: 3 }}>
                <CardContent>
                    <Box 
                        sx={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            mb: filter ? 2 : 0,
                            cursor: 'pointer',
                            userSelect: 'none'
                        }}
                        onClick={() => setFilter(!filter)}
                    >
                        <FilterListIcon sx={{ mr: 1 }} />
                        <Typography variant="h6">Filter & Sortierung</Typography>
                        {filter ? <ExpandLessIcon sx={{ ml: 1 }} /> : <ExpandMoreIcon sx={{ ml: 1 }} />}
                        <Button
                            startIcon={<ClearIcon />}
                            onClick={(e) => {
                                e.stopPropagation(); // Verhindert das Zuklappen beim Klick auf den Reset-Button
                                clearFilters();
                            }}
                            sx={{ ml: 'auto' }}
                            variant="outlined"
                            size="small"
                        >
                            Filter zurücksetzen
                        </Button>
                    </Box>
                    <Collapse in={filter} timeout="auto" unmountOnExit>
                        <Stack spacing={2}>
                            {/* First Row - Status and Employee */}
                            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                                <FormControl sx={{ minWidth: 150 }} size="small">
                                    <InputLabel>Status</InputLabel>
                                    <Select
                                        value={statusFilter}
                                        label="Status"
                                        onChange={(e) => setStatusFilter(e.target.value)}
                                    >
                                        <MenuItem value="alle">Alle Status</MenuItem>
                                        <MenuItem value="pending">Ausstehend</MenuItem>
                                        <MenuItem value="genehmigt">Genehmigt</MenuItem>
                                        <MenuItem value="abgelehnt">Abgelehnt</MenuItem>
                                    </Select>
                                </FormControl>

                                <FormControl sx={{ minWidth: 200 }} size="small">
                                    <InputLabel>Mitarbeiter</InputLabel>
                                    <Select
                                        value={employeeFilter}
                                        label="Mitarbeiter"
                                        onChange={(e) => setEmployeeFilter(e.target.value)}
                                    >
                                        <MenuItem value="alle">Alle Mitarbeiter</MenuItem>
                                        {Array.from(employees.values()).map((employee) => (
                                            <MenuItem key={employee.employeeId} value={employee.employeeId.toString()}>
                                                {EmployeeService.formatEmployeeName(employee)}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Box>

                            {/* Second Row - Date Filter */}
                            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                                <DatePicker
                                    label="Datum filtern"
                                    value={dateFilter}
                                    onChange={(newValue) => setDateFilter(newValue)}
                                    slotProps={{ 
                                        textField: { 
                                            size: 'small',
                                            sx: { minWidth: 200 },
                                            helperText: 'Zeigt Anträge, die an diesem Datum aktiv sind'
                                        } 
                                    }}
                                />
                            </Box>

                            {/* Third Row - Sorting */}
                            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                                <FormControl sx={{ minWidth: 150 }} size="small">
                                    <InputLabel>Sortieren nach</InputLabel>
                                    <Select
                                        value={sortBy}
                                        label="Sortieren nach"
                                        onChange={(e) => setSortBy(e.target.value)}
                                    >
                                        <MenuItem value="submittedAt">Eingereicht am</MenuItem>
                                        <MenuItem value="startDatum">Startdatum</MenuItem>
                                        <MenuItem value="employee">Mitarbeiter</MenuItem>
                                        <MenuItem value="status">Status</MenuItem>
                                    </Select>
                                </FormControl>

                                <FormControl sx={{ minWidth: 120 }} size="small">
                                    <InputLabel>Reihenfolge</InputLabel>
                                    <Select
                                        value={sortOrder}
                                        label="Reihenfolge"
                                        onChange={(e) => setSortOrder(e.target.value as 'asc' | 'desc')}
                                    >
                                        <MenuItem value="asc">Aufsteigend</MenuItem>
                                        <MenuItem value="desc">Absteigend</MenuItem>
                                    </Select>
                                </FormControl>
                            </Box>

                            {/* Filter Summary */}
                            <Typography variant="body2" color="text.secondary">
                                {filteredAntraege.length} von {antraege.length} Anträgen angezeigt
                            </Typography>
                        </Stack>
                    </Collapse>
                </CardContent>
            </Card>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Mitarbeiter</TableCell>
                            <TableCell>Zeitraum</TableCell>
                            <TableCell>Tage</TableCell>
                            <TableCell>Art</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell>Eingereicht am</TableCell>
                            <TableCell align="center">Aktionen</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {filteredAntraege.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={7} align="center">
                                    {antraege.length === 0 ? 'Keine Anträge bisher' : 'Keine Anträge entsprechen den Filterkriterien'}
                                </TableCell>
                            </TableRow>
                        ) : (
                            filteredAntraege.map((antrag, index) => (
                                <TableRow key={antrag.antragsId || `temp-${index}`} hover>
                                    <TableCell>{getEmployeeName(antrag.employeeId)}</TableCell>
                                    <TableCell>
                                        {formatDate(antrag.startDatum)} - {formatDate(antrag.endDatum)}
                                    </TableCell>
                                    <TableCell>
                                        {calculateDays(antrag.startDatum, antrag.endDatum)}
                                    </TableCell>
                                    <TableCell>{antrag.type}</TableCell>
                                    <TableCell>{getStatusChip(antrag.status)}</TableCell>
                                    <TableCell>{formatDateTime(antrag.submittedAt)}</TableCell>
                                    <TableCell align="center">
                                        <Tooltip title="Details anzeigen">
                                            <IconButton
                                                onClick={() => handleViewDetails(antrag)}
                                                size="small"
                                            >
                                                <VisibilityIcon />
                                            </IconButton>
                                        </Tooltip>
                                        {antrag.status === 'pending' && antrag.antragsId && (accessRights.includes("urlaub.review") || accessRights.includes("admin")) && (
                                            <Tooltip title="Bearbeiten">
                                                <IconButton
                                                    onClick={() => handleOpenReviewDialog(antrag)}
                                                    size="small"
                                                >
                                                    <BuildSharp />
                                                </IconButton>
                                            </Tooltip>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            {/* Details Dialog */}
            <Dialog
                open={detailsOpen}
                onClose={handleCloseDetails}
                maxWidth="md"
                fullWidth
            >
                <DialogTitle>
                    Urlaubsantrag Details - {selectedAntrag ? getEmployeeName(selectedAntrag.employeeId) : 'Unbekannt'}
                </DialogTitle>
                <DialogContent>
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

                    {selectedAntrag && (
                        <Box sx={{ mt: 2 }}>
                            <Typography variant="h6" gutterBottom>
                                Antragsdetails
                            </Typography>

                            <Box sx={{ mb: 2 }}>
                                <Typography><strong>Mitarbeiter:</strong> {getEmployeeName(selectedAntrag.employeeId)}</Typography>
                                <Typography><strong>Zeitraum:</strong> {formatDate(selectedAntrag.startDatum)} - {formatDate(selectedAntrag.endDatum)}</Typography>
                                <Typography><strong>Anzahl Tage:</strong> {calculateDays(selectedAntrag.startDatum, selectedAntrag.endDatum)}</Typography>
                                <Typography><strong>Art:</strong> {selectedAntrag.type}</Typography>
                                <Typography><strong>Status:</strong> {getStatusChip(selectedAntrag.status)}</Typography>
                                <Typography><strong>Eingereicht am:</strong> {formatDateTime(selectedAntrag.submittedAt)}</Typography>
                            </Box>

                            {selectedAntrag.comment && (
                                <Box sx={{ mb: 2 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Kommentar des Mitarbeiters
                                    </Typography>
                                    <Typography>{selectedAntrag.comment}</Typography>
                                </Box>
                            )}

                            {selectedAntrag.status !== 'pending' && (
                                <Box sx={{ mb: 2 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Bewertung
                                    </Typography>
                                    <Typography><strong>Bearbeitet von:</strong> {getEmployeeName(selectedAntrag.reviewerId || null)}</Typography>
                                    <Typography><strong>Bearbeitet am:</strong> {selectedAntrag.reviewDate && formatDateTime(selectedAntrag.reviewDate)}</Typography>
                                    <Typography><strong>Status:</strong> {selectedAntrag.status === 'genehmigt' ? '✅ Genehmigt' : '❌ Abgelehnt'}</Typography>
                                    {selectedAntrag.reviewComment && (
                                        <Typography><strong>Kommentar:</strong> {selectedAntrag.reviewComment}</Typography>
                                    )}
                                </Box>
                            )}

                            {selectedAntrag.status === 'pending' && (accessRights.includes("urlaub.review") || accessRights.includes("admin")) && (
                                <Box sx={{ mt: 3 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Bewertung hinzufügen
                                    </Typography>
                                    <TextField
                                        fullWidth
                                        multiline
                                        rows={3}
                                        label="Kommentar zur Bewertung (optional)"
                                        value={reviewComment}
                                        onChange={(e) => setReviewComment(e.target.value)}
                                        placeholder="Grund für Genehmigung/Ablehnung..."
                                    />
                                </Box>
                            )}
                        </Box>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDetails} disabled={loading}>
                        Schließen
                    </Button>
                    {selectedAntrag?.status === 'pending' && (accessRights.includes("urlaub.review") || accessRights.includes("admin")) && (
                        <>
                            <Button
                                onClick={() => handleReviewAntrag('REJECTED')}
                                color="error"
                                variant="outlined"
                                disabled={loading || !selectedAntrag?.antragsId}
                            >
                                {loading ? 'Wird bearbeitet...' : 'Ablehnen'}
                            </Button>
                            <Button
                                onClick={() => handleReviewAntrag('APPROVED')}
                                color="success"
                                variant="contained"
                                disabled={loading || !selectedAntrag?.antragsId}
                            >
                                {loading ? 'Wird bearbeitet...' : 'Genehmigen'}
                            </Button>
                        </>
                    )}
                </DialogActions>
            </Dialog>
        </Box>
        </LocalizationProvider>
    );
};

export default AntragTabelle;
