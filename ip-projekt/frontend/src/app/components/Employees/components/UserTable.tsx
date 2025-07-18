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
import { Employee } from '../../services/employeeService';
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

interface UserTableProps {
    users: Employee[];
    onEditUser: (user: Employee) => void;
    onDeleteUser: (user: Employee) => void;
}

const UserTable: React.FC<UserTableProps> = ({ users, onEditUser, onDeleteUser }) => {
    const { accessRights } = useAuth();
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', p: 3, width: '100%', maxWidth: 1200, mx: 'auto' }}>
            <StyledTablePaper>
                <TableContainer>
                    <Table aria-label="users table">
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ fontWeight: 'bold' }}>ID</TableCell>
                                <TableCell sx={{ fontWeight: 'bold' }}>Nachname</TableCell>
                                <TableCell sx={{ fontWeight: 'bold' }}>Vorname</TableCell>
                                <TableCell sx={{ fontWeight: 'bold' }}>Rolle</TableCell>
                                {(accessRights.includes("user.update") || accessRights.includes("user.delete") || accessRights.includes("admin")) && (
                                    <TableCell sx={{ fontWeight: 'bold' }} align="right">Aktionen</TableCell>
                                )}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {users.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4} align="center">Noch keine Mitarbeiter erstellt.</TableCell>
                                </TableRow>
                            ) : (
                                users.map((user) => (
                                    <TableRow
                                        hover
                                        role="checkbox"
                                        tabIndex={-1}
                                        key={user.employeeId}
                                        sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                                    >
                                        <TableCell component="th" scope="row">
                                            {user.employeeId}
                                        </TableCell>
                                        <TableCell>{user.firstName}</TableCell>
                                        <TableCell>{user.lastName}</TableCell>
                                        <TableCell>{user.role ? user.role.roleName : <p style={{ fontWeight: "bold" }}>Keine Rolle</p>}</TableCell>
                                        {(accessRights.includes("user.update") || accessRights.includes("user.delete") || accessRights.includes("admin")) && (
                                            <TableCell align="right">
                                                {(accessRights.includes("user.update") || accessRights.includes("admin")) && (
                                                    <IconButton
                                                        onClick={() => onEditUser(user)}
                                                        color="primary"
                                                        size="small"
                                                        title='Bearbeiten'
                                                    >
                                                        <EditIcon />
                                                    </IconButton>
                                                )}
                                                {(accessRights.includes("user.delete") || accessRights.includes("admin")) && (
                                                    <IconButton
                                                        onClick={() => onDeleteUser(user)}
                                                        color="error"
                                                        size="small"
                                                        title='Löschen'
                                                    >
                                                        <DeleteIcon />
                                                    </IconButton>
                                                )}
                                            </TableCell>
                                        )}
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

export default UserTable; 