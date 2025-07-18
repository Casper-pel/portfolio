import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import Paper from '@mui/material/Paper';
import { modalStyle } from '../../model/modalStyle';

interface DeleteConfirmationModalProps {
    open: boolean;
    onClose: () => void;
    onConfirm: () => void;
    roleName: string;
}

const DeleteConfirmationModal: React.FC<DeleteConfirmationModalProps> = ({
    open,
    onClose,
    onConfirm,
    roleName
}) => {
    return (
        <Modal
            open={open}
            onClose={onClose}
            aria-labelledby="delete-modal-title"
            aria-describedby="delete-modal-description"
        >
            <Paper sx={{
                ...modalStyle,
                p: 3,
                width: '400px'
            }}>
                <Typography id="delete-modal-title" variant="h6" component="h2" gutterBottom>
                    Bestätigung löschen
                </Typography>
                <Typography id="delete-modal-description" sx={{ mt: 2, mb: 3 }}>
                    Sind Sie sicher, dass Sie die Rolle löschen möchten: <strong>{roleName}</strong>? Diese Aktion kann nicht rückgängig gemacht werden.
                </Typography>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                    <Button variant="outlined" onClick={onClose}>
                        Abbrechen
                    </Button>
                    <Button variant="contained" color="error" onClick={onConfirm}>
                        Löschen
                    </Button>
                </Box>
            </Paper>
        </Modal>
    );
};

export default DeleteConfirmationModal; 