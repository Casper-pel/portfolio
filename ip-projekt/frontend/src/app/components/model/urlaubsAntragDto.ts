export interface UrlaubsAntragDto {
    antragsId?: number;
    employeeId: number;
    startDatum: string;
    endDatum: string;
    status: 'pending' | 'genehmigt' | 'abgelehnt';
    type: string;
    grund: string;
    reviewDate?: string | null; // Optional beim Erstellen, wird erst beim Review gesetzt
    reviewerId?: number | null; // Optional beim Erstellen, wird erst beim Review gesetzt
    comment?: string;
}