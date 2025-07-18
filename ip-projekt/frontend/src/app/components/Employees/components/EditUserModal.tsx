import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import {
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Paper
} from '@mui/material';
import { modalStyle } from '../../model/modalStyle';
import { Role } from '../../model/role';

interface EditUserModalProps {
    open: boolean;
    onClose: () => void;
    onSave: () => void;
    userId: number;
    setUserId: (userId: string) => void;
    firstName: string;
    setFirstName: (firstName: string) => void;
    lastName: string;
    setLastName: (lastName: string) => void;
    password: string;
    setPassword: (password: string) => void;
    selectedRole: string;
    setSelectedRole: (role: string) => void;
    roles: Role[];
    isEditing: boolean;
    isDisabled: boolean;
}

const EditUserModal: React.FC<EditUserModalProps> = ({
    open,
    onClose,
    onSave,
    userId,
    setUserId,
    firstName,
    setFirstName,
    lastName,
    setLastName,
    password,
    setPassword,
    selectedRole,
    setSelectedRole,
    roles,
    isEditing,
    isDisabled
}) => {
    return (
        <Modal
            open={open}
            onClose={onClose}
            aria-labelledby="edit-user-modal-title"
            aria-describedby="edit-user-modal-description"
        >
            <Paper sx={{
                ...modalStyle,
                maxHeight: '90vh',
                overflowY: 'auto',
                p: 3,
                width: '600px'
            }}>
                <Typography id="edit-user-modal-title" variant="h6" component="h2" gutterBottom>
                    {isEditing ? 'Mitarbeiter bearbeiten' : 'Neuen Mitarbeiter erstellen'}
                </Typography>
                <Box sx={{ mt: 2 }}>
                    <TextField
                        required
                        id="userId"
                        label="Benutzer-ID"
                        variant="outlined"
                        fullWidth
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                        sx={{ mb: 2 }}
                        disabled={isEditing}
                        helperText={isEditing ? "ID kann beim Bearbeiten nicht geändert werden" : "Eindeutige Benutzer-ID eingeben"}
                    />
                    <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                        <TextField
                            required
                            id="firstName"
                            label="Vorname"
                            variant="outlined"
                            fullWidth
                            value={firstName}
                            onChange={(e) => setFirstName ? setFirstName(e.target.value) : undefined}
                        />
                        <TextField
                            required
                            id="lastName"
                            label="Nachname"
                            variant="outlined"
                            fullWidth
                            value={lastName}
                            onChange={(e) => setLastName ? setLastName(e.target.value) : undefined}
                        />
                    </Box>
                    <TextField
                        id="password"
                        label={isEditing ? "Passwort (optional - leer lassen um beizubehalten)" : "Passwort"}
                        type="password"
                        variant="outlined"
                        fullWidth
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        sx={{ mb: 2 }}
                        required={!isEditing}
                        helperText={isEditing ? "Leer lassen, um das aktuelle Passwort zu behalten" : ""}
                    />
                    <FormControl fullWidth sx={{ mb: 2 }}>
                        <InputLabel id="role-select-label">Rolle (optional)</InputLabel>
                        <Select
                            labelId="role-select-label"
                            id="role-select"
                            value={selectedRole}
                            label="Rolle (optional)"
                            onChange={(e) => setSelectedRole(e.target.value)}
                        >
                            <MenuItem value="">
                                <em>Keine Rolle zuweisen</em>
                            </MenuItem>
                            {roles.map((role) => (
                                <MenuItem
                                    key={role.roleId ?? ''}
                                    value={(role.roleId ?? '').toString()}
                                >
                                    {role.roleName}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Box>

                <Button
                    sx={{ mt: 2, width: "100%" }}
                    variant="contained"
                    onClick={onSave}
                    disabled={isDisabled}
                >
                    {isEditing ? 'Aktualisieren' : 'Hinzufügen'}
                </Button>
            </Paper>
        </Modal>
    );
};

export default EditUserModal; 