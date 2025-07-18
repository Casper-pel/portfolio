"use client";

import { useState, useEffect } from 'react';
import { Box, Button, Typography, Alert } from '@mui/material';
import UserTable from './components/UserTable';
import DeleteConfirmationModal from './components/DeleteConfirmationModal';
import EditUserModal from './components/EditUserModal';
import { useRouter } from 'next/navigation';
import { Role } from '../model/role';
import { Employee, EmployeeService } from '../services/employeeService';
import { RoleService } from '../services/roleService';
import { useAuth } from '@/app/context/AuthContext';

const Employees = () => {
    const router = useRouter();
    const { accessRights } = useAuth();

    // States für Nutzer, Rollen und Fehler und tracking ob Modal geöffnet ist
    const [users, setUsers] = useState<Employee[]>([]);
    const [roles, setRoles] = useState<Role[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<Employee | null>(null);
    const [userToDelete, setUserToDelete] = useState<Employee | null>(null);

    // Form states bei speichern/bearbeiten von Nutzern; in Modal benötigt
    const [userId, setUserId] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [password, setPassword] = useState('');
    const [selectedRole, setSelectedRole] = useState<string>('');

    // Prüfung ob Nutzer berechtigt ist, um auf diese Seite zuzugreifen
    useEffect(() => {
        if (accessRights.length > 0 && !accessRights.includes("user.read") && !accessRights.includes("admin")) {
            window.location.href = "/dashboard";
        }
    }, [accessRights]);

    useEffect(() => {
        fetchUsers();
        fetchRoles();
    }, []);

    useEffect(() => {
        if (editingUser) {
            setUserId(editingUser.employeeId.toString());
            setFirstName(editingUser.firstName);
            setLastName(editingUser.lastName);
            setPassword('');
            setSelectedRole(editingUser.role && editingUser.role.roleId ? editingUser.role.roleId.toString() : '');
        } else {
            setUserId('');
            setFirstName('');
            setLastName('');
            setPassword('');
            setSelectedRole('');
        }
    }, [editingUser]);

    const fetchUsers = async () => {
        try {
            const data = await EmployeeService.getAllEmployees();
            setUsers(data);
        } catch (err) {
            setError('Fehler beim Laden der Mitarbeiter');
            console.error('Error fetching users:', err);
        }
    };

    const fetchRoles = async () => {
        try {
            const data = await RoleService.getAllRoles();
            setRoles(data.map(role => ({
                ...role,
                employees: [] // Replace with an empty array or a valid property if applicable
            })));
        } catch (err) {
            setError('Fehler beim Laden der Rollen');
            console.error('Error fetching roles:', err);
        }
    };

    const handleOpenModal = () => {
        router.push("/signup");
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingUser(null);
        setUserId('');
        setFirstName('');
        setLastName('');
        setPassword('');
        setSelectedRole('');
    };

    const handleEditUser = (user: Employee) => {
        setEditingUser(user);
        setIsModalOpen(true);
    };

    const handleDeleteUser = (user: Employee) => {
        setUserToDelete(user);
        setIsDeleteModalOpen(true);
    };

    const handleSaveUser = async () => {
        if (editingUser) {
            const userData = {
                employeeId: editingUser.employeeId,
                firstName,
                lastName,
                password,
                role: selectedRole ? roles.find(r => r.roleId && r.roleId.toString() === selectedRole) || null : null
            };

            try {
                await EmployeeService.updateEmployee(userData);
                await fetchUsers();
                await fetchRoles();
                handleCloseModal();
                setError(null);
            } catch (err) {
                setError('Fehler beim Aktualisieren des Mitarbeiters');
                console.error('Error updating user:', err);
            }
        } else {
            setError('Neue Mitarbeiter müssen über die Registrierungsseite erstellt werden');
        }
    };

    const handleDeleteConfirm = async () => {
        if (!userToDelete) return;

        try {
            await EmployeeService.deleteEmployee(userToDelete.employeeId);
            await fetchUsers();
            await fetchRoles();
            setIsDeleteModalOpen(false);
            setUserToDelete(null);
            setError(null);
        } catch (err: any) {
            if (err.message.includes('Forbidden')) {
                setError("Sie können den Account von " + userToDelete.firstName + ' ' + userToDelete.lastName + " nicht löschen");
            } else {
                setError('Fehler beim Löschen des Mitarbeiters');
            }
            setIsDeleteModalOpen(false);
            console.error('Error deleting user:', err);
        }
    };

    return (
        <Box sx={{
            p: 3,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            width: '100%',
            maxWidth: 1200,
            mx: 'auto'
        }}>
            <Typography variant="h4" gutterBottom align="center" sx={{ width: '100%' }}>
                Mitarbeiterverwaltung
            </Typography>

            {error && (
                <Alert severity="error" sx={{ mb: 2, width: '100%' }}>
                    {error}
                </Alert>
            )}

            {(accessRights.includes("user.create") || accessRights.includes("admin")) && (
                <Button
                    variant="contained"
                    onClick={handleOpenModal}
                    sx={{ mb: 3 }}
                >
                    Neuen Mitarbeiter erstellen
                </Button>
            )}

            <UserTable
                users={users}
                onEditUser={handleEditUser}
                onDeleteUser={handleDeleteUser}
            />

            <EditUserModal
                open={isModalOpen}
                onClose={handleCloseModal}
                onSave={handleSaveUser}
                userId={parseInt(userId) || 0}
                setUserId={setUserId}
                firstName={firstName}
                setFirstName={setFirstName}
                lastName={lastName}
                setLastName={setLastName}
                password={password}
                setPassword={setPassword}
                selectedRole={selectedRole}
                setSelectedRole={setSelectedRole}
                roles={roles}
                isEditing={!!editingUser}
                isDisabled={!firstName || !lastName || (!editingUser && (!password || !userId))}
            />

            {userToDelete && (
                <DeleteConfirmationModal
                    open={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onConfirm={handleDeleteConfirm}
                    username={userToDelete.firstName + ' ' + userToDelete.lastName}
                />
            )}
        </Box>
    );
};

export default Employees; 