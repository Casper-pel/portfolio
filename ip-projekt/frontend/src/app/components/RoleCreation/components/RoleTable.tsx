import Box from '@mui/material/Box';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { styled } from '@mui/material/styles';
import { Role } from '../../services/roleService';
import { useAuth } from '@/app/context/AuthContext';

const StyledTablePaper = styled(Box)(({ theme }) => ({
    marginTop: theme.spacing(3),
    marginBottom: theme.spacing(3),
    borderRadius: theme.shape.borderRadius,
    boxShadow: theme.shadows[3],
    backgroundColor: theme.palette.background.paper,
    width: '100%',
    overflowX: 'auto',
}));

interface RoleTableProps {
    roles: Role[];
    onEditRole: (role: Role) => void;
    onDeleteRole: (role: Role) => void;
}

const truncateText = (text: string, maxLength: number = 50): string => {
    if (text.length <= maxLength) {
        return text;
    }
    return text.substring(0, maxLength) + '...';
};

const RoleTable: React.FC<RoleTableProps> = ({ roles, onEditRole, onDeleteRole }) => {
    const { accessRights } = useAuth();

    const truncatedCellStyle = {
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        maxWidth: 150,
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 3, width: '100%', maxWidth: 1200, mx: 'auto' }}>
            <StyledTablePaper>
                <TableContainer>
                    <Table aria-label="roles table">
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ fontWeight: 'bold' }}>Rollenname</TableCell>
                                <TableCell sx={{ fontWeight: 'bold', ...truncatedCellStyle }}>Beschreibung</TableCell>
                                <TableCell sx={{ fontWeight: 'bold', ...truncatedCellStyle }}>Berechtigungen</TableCell>
                                {(accessRights.includes("role.update") || accessRights.includes("role.delete") || accessRights.includes("admin")) && (
                                    <TableCell sx={{ fontWeight: 'bold' }} align="right">Aktionen</TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {roles.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4} align="center">Noch keine Rollen erstellt.</TableCell>
                                </TableRow>
                            ) : (
                                roles.map((role) => (
                                    <TableRow
                                        hover
                                        role="checkbox"
                                        tabIndex={-1}
                                        key={role.roleId}
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >
                                        <TableCell component="th" scope="row">
                                            {role.roleName}
                                        </TableCell>
                                        <TableCell sx={truncatedCellStyle}>
                                            {truncateText(role.description, 100)}
                                        </TableCell>
                                        <TableCell sx={truncatedCellStyle}>
                                            {truncateText((role.rolePermissions || []).join(', ') || 'Keine Berechtigungen', 100)}
                                        </TableCell>
                                        <TableCell align="right">
                                            {(accessRights.includes("role.update") || accessRights.includes("admin")) && (
                                                <IconButton
                                                    onClick={() => onEditRole(role)}
                                                    color="primary"
                                                    size="small"
                                                >
                                                    <EditIcon />
                                                </IconButton>
                                            )}
                                            {(accessRights.includes("role.delete") || accessRights.includes("admin")) && (
                                                <IconButton
                                                    onClick={() => onDeleteRole(role)}
                                                    color="error"
                                                    size="small"
                                                >
                                                    <DeleteIcon />
                                                </IconButton>
                                            )}
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </StyledTablePaper>
        </Box>
    );
};

export default RoleTable; 