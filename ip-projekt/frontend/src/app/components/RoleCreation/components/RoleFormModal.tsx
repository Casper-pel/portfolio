import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import {
    TextField,
    Checkbox,
    FormControlLabel,
    FormLabel,
    Paper
} from '@mui/material';
import { modalStyle } from '../../model/modalStyle';

interface RoleFormModalProps {
    open: boolean;
    onClose: () => void;
    onSave: () => void;
    roleName: string;
    setRoleName: (name: string) => void;
    roleDescription: string;
    setRoleDescription: (description: string) => void;
    selectedPermissions: string[];
    setSelectedPermissions: (permissions: string[]) => void;
    isEditing: boolean;
    isDisabled: boolean;
}

const RoleFormModal: React.FC<RoleFormModalProps> = ({
    open,
    onClose,
    onSave,
    roleName,
    setRoleName,
    roleDescription,
    setRoleDescription,
    selectedPermissions,
    setSelectedPermissions,
    isEditing,
    isDisabled
}) => {
    const handlePermissionChange = (permission: string) => {
        const permissionExists = selectedPermissions.includes(permission);

        if (!permissionExists) {
            // Füge die ausgewählte Permission hinzu
            let newPermissions = [...selectedPermissions, permission];

            // Automatisch "read" hinzufügen wenn create, update oder delete ausgewählt wird
            if (permission.endsWith('.create') || permission.endsWith('.update') || permission.endsWith('.delete')) {
                const basePermission = permission.substring(0, permission.lastIndexOf('.'));
                const readPermission = basePermission + '.read';

                if (!newPermissions.includes(readPermission)) {
                    newPermissions.push(readPermission);
                }
            }

            // Spezielle Regel: Wenn Kassen Recht hat, automatisch produkte.read hinzufügen
            if (permission === 'kasse' && !newPermissions.includes('product.read')) {
                newPermissions.push('product.read');
            }

            // Spezielle Regel: Wenn Finanzen Recht hat, automatisch user.read hinzufügen
            if (permission === "finances" && !newPermissions.includes("user.read")) {
                newPermissions.push("user.read");
            }

            setSelectedPermissions(newPermissions);
        } else {
            // Entferne die Permission
            let newPermissions = selectedPermissions.filter(p => p !== permission);

            // Wenn "read" entfernt wird, entferne auch create, update und delete
            if (permission.endsWith('.read')) {
                const basePermission = permission.substring(0, permission.lastIndexOf('.'));
                newPermissions = newPermissions.filter(p =>
                    !p.startsWith(basePermission + '.create') &&
                    !p.startsWith(basePermission + '.update') &&
                    !p.startsWith(basePermission + '.delete')
                );
            }

            if (permission === "user.read") {
                newPermissions = newPermissions.filter(p => p !== "finances");
            }

            // Wenn product.read entfernt wird, entferne auch kasse
            if (permission === 'product.read') {
                newPermissions = newPermissions.filter(p => p !== 'kasse');
            }

            setSelectedPermissions(newPermissions);
        }
    };

    return (
        <Modal
            open={open}
            onClose={onClose}
            aria-labelledby="role-form-modal-title"
            aria-describedby="role-form-modal-description"
        >
            <Paper sx={{
                ...modalStyle,
                maxHeight: '90vh',
                overflowY: 'auto',
                p: 3,
                width: '600px'
            }}>
                <Typography id="role-form-modal-title" variant="h6" component="h2" gutterBottom>
                    {isEditing ? 'Rolle bearbeiten' : 'Neue Rolle erstellen'}
                </Typography>
                <Box sx={{ mt: 2 }}>
                    <TextField
                        required
                        id="role-name"
                        label="Name"
                        variant="outlined"
                        fullWidth
                        value={roleName}
                        onChange={(e) => setRoleName(e.target.value)}
                        sx={{ mb: 2 }}
                    />
                    <TextField
                        id="role-description"
                        label="Beschreibung"
                        variant="outlined"
                        fullWidth
                        multiline
                        rows={2}
                        value={roleDescription}
                        onChange={(e) => setRoleDescription(e.target.value)}
                        sx={{ mb: 2 }}
                    />
                </Box>

                <Box>
                    <Typography id="rights-title" variant="h6" sx={{ mt: 3, mb: 2, fontWeight: 'bold' }}>
                        Berechtigungen:
                    </Typography>

                    <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
                        <Typography variant="subtitle1" sx={{ color: 'primary.main', mb: 1 }}>
                            Globale Berechtigungen
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                            <FormControlLabel
                                id='admin-permission'
                                control={
                                    <Checkbox
                                        checked={selectedPermissions.includes("admin")}
                                        onChange={() => handlePermissionChange("admin")}
                                    />
                                }
                                label="Administrator"
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={selectedPermissions.includes("kasse")}
                                        onChange={() => handlePermissionChange("kasse")}
                                    />
                                }
                                label="Kassenzugang"
                            />
                        </Box>
                    </Paper>

                    <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
                        <Typography variant="subtitle1" sx={{ color: 'primary.main', mb: 1 }}>
                            Benutzer & Rollen
                        </Typography>
                        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2 }}>
                            <Box>
                                <Typography variant="body2" sx={{ fontWeight: 'medium', mb: 1 }}>Benutzer</Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("user.read")}
                                                onChange={() => handlePermissionChange("user.read")}
                                            />
                                        }
                                        label="Ansehen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("user.create")}
                                                onChange={() => handlePermissionChange("user.create")}
                                            />
                                        }
                                        label="Erstellen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("user.update")}
                                                onChange={() => handlePermissionChange("user.update")}
                                            />
                                        }
                                        label="Bearbeiten"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("user.delete")}
                                                onChange={() => handlePermissionChange("user.delete")}
                                            />
                                        }
                                        label="Löschen"
                                    />
                                </Box>
                            </Box>
                            <Box>
                                <Typography variant="body2" sx={{ mb: 1, fontWeight: 'medium' }}>Rollen</Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("role.read")}
                                                onChange={() => handlePermissionChange("role.read")}
                                            />
                                        }
                                        label="Ansehen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("role.create")}
                                                onChange={() => handlePermissionChange("role.create")}
                                            />
                                        }
                                        label="Erstellen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("role.update")}
                                                onChange={() => handlePermissionChange("role.update")}
                                            />
                                        }
                                        label="Bearbeiten"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("role.delete")}
                                                onChange={() => handlePermissionChange("role.delete")}
                                            />
                                        }
                                        label="Löschen"
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Paper>

                    <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
                        <Typography variant="subtitle1" sx={{ color: 'primary.main', mb: 1 }}>
                            Geschäftsbereiche
                        </Typography>
                        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 2 }}>
                            <Box>
                                <Typography variant="body2" sx={{ mb: 1, fontWeight: 'medium' }}>Produkte</Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("product.read")}
                                                onChange={() => handlePermissionChange("product.read")}
                                            />
                                        }
                                        label="Ansehen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("product.create")}
                                                onChange={() => handlePermissionChange("product.create")}
                                            />
                                        }
                                        label="Erstellen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("product.update")}
                                                onChange={() => handlePermissionChange("product.update")}
                                            />
                                        }
                                        label="Bearbeiten"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("product.delete")}
                                                onChange={() => handlePermissionChange("product.delete")}
                                            />
                                        }
                                        label="Löschen"
                                    />
                                </Box>
                            </Box>
                            <Box>
                                <Typography variant="body2" sx={{ mb: 1, fontWeight: 'medium' }}>Coupons</Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("coupons.read")}
                                                onChange={() => handlePermissionChange("coupons.read")}
                                            />
                                        }
                                        label="Lesen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("coupons.create")}
                                                onChange={() => handlePermissionChange("coupons.create")}
                                            />
                                        }
                                        label="Erstellen"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("coupons.update")}
                                                onChange={() => handlePermissionChange("coupons.update")}
                                            />
                                        }
                                        label="Updaten"
                                    />
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("coupons.delete")}
                                                onChange={() => handlePermissionChange("coupons.delete")}
                                            />
                                        }
                                        label="Löschen"
                                    />
                                </Box>
                            </Box>
                            <Box>
                                <Typography variant="body2" sx={{ mb: 1, fontWeight: 'medium' }}>Finanzen</Typography>
                                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                    <FormControlLabel
                                        control={
                                            <Checkbox
                                                checked={selectedPermissions.includes("finances")}
                                                onChange={() => handlePermissionChange("finances")}
                                            />
                                        }
                                        label="Zugang"
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Paper>

                    <Paper elevation={1} sx={{ p: 2, mb: 2 }}>
                        <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 'medium', color: 'primary.main' }}>
                            Urlaub & Personal
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={selectedPermissions.includes("urlaub.read")}
                                        onChange={() => handlePermissionChange("urlaub.read")}
                                    />
                                }
                                label="Urlaub ansehen"
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={selectedPermissions.includes("urlaub.review")}
                                        onChange={() => handlePermissionChange("urlaub.review")}
                                    />
                                }
                                label="Urlaub genehmigen"
                            />
                        </Box>
                    </Paper>
                </Box>

                <Button
                    sx={{ mt: 2, width: "100%" }}
                    variant="contained"
                    onClick={onSave}
                    disabled={isDisabled}
                >
                    {isEditing ? 'Rolle aktualisieren' : 'Hinzufügen'}
                </Button>
            </Paper>
        </Modal>
    );
};

export default RoleFormModal; 