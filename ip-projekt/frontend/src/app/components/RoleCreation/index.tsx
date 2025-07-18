"use client";

import { useState, useEffect } from 'react';
import { Box, Button, Typography, Alert } from '@mui/material';
import RoleTable from './components/RoleTable';
import RoleFormModal from './components/RoleFormModal';
import DeleteConfirmationModal from './components/DeleteConfirmationModal';
import { Role } from '../services/roleService';
import { API_BASE_URL } from '../requests/baseUrl';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/app/context/AuthContext';

const RoleCreation = () => {
    const {accessRights} = useAuth();
    const router = useRouter();

    const [roles, setRoles] = useState<Role[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [editingRole, setEditingRole] = useState<Role | null>(null);
    const [roleToDelete, setRoleToDelete] = useState<Role | null>(null);

    // Form states zum tracken der Eingaben bei der Rollenerstellung und -bearbeitung
    const [roleName, setRoleName] = useState('');
    const [roleDescription, setRoleDescription] = useState('');
    const [selectedPermissions, setSelectedPermissions] = useState<string[]>([]);

    useEffect(() => {
        if(accessRights.length > 0 && !accessRights.includes("role.read") && !accessRights.includes("admin")) {
            router.push('/dashboard');
        }
    })

    useEffect(() => {
        fetchRoles();
    }, []);

    useEffect(() => {
        if (editingRole) {
            setRoleName(editingRole.roleName);
            setRoleDescription(editingRole.description);
            setSelectedPermissions(editingRole.rolePermissions);
        } else {
            setRoleName('');
            setRoleDescription('');
            setSelectedPermissions([]);
        }
    }, [editingRole]);

    const fetchRoles = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/role/all`, {
                credentials: 'include',
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch roles');
            }
            if (response.status === 204) {
                setRoles([]);
                return;
            }
            const data = await response.json();
            setRoles(data);
        } catch (err) {
            setError('Fehler beim Laden der Rollen');
            console.error('Error fetching roles:', err);
        }
    };

    const handleOpenModal = () => {
        setEditingRole(null);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingRole(null);
        setRoleName('');
        setRoleDescription('');
        setSelectedPermissions([]);
    };

    const handleEditRole = (role: Role) => {
        setEditingRole(role);
        setIsModalOpen(true);
    };

    const handleDeleteRole = (role: Role) => {
        setRoleToDelete(role);
        setIsDeleteModalOpen(true);
    };

    const handleSaveRole = async () => {
        // Sende nur die Basis-Rolleninformationen - keine Mitarbeiter
        const roleData = {
            roleId: editingRole ? editingRole.roleId : Math.floor(Math.random() * 1000),
            roleName,
            description: roleDescription,
            rolePermissions: selectedPermissions,
        };

        try {
            const response = await fetch(
                `${API_BASE_URL}/role/${editingRole ? 'update' : 'add'}`,
                {
                    credentials: 'include',
                    method: editingRole ? 'PUT' : 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(roleData),
                }
            );

            if (!response.ok) {
                throw new Error('Failed to save role');
            }

            await fetchRoles();
            handleCloseModal();
            setError(null);
            window.location.reload();
        } catch (err) {
            setError('Fehler beim Speichern der Rolle');
            console.error('Error saving role:', err);
        }
    };

    const handleDeleteConfirm = async () => {
        if (!roleToDelete) return;

        try {
            const response = await fetch(`${API_BASE_URL}/role/delete/${roleToDelete.roleId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to delete role');
            }

            await fetchRoles();
            setIsDeleteModalOpen(false);
            setRoleToDelete(null);
            setError(null);
        } catch (err) {
            setError('Fehler beim Löschen der Rolle');
            console.error('Error deleting role:', err);
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
                Rollenverwaltung
            </Typography>

            {error && (
                <Alert severity="error" sx={{ mb: 2, width: '100%' }}>
                    {error}
                </Alert>
            )}

            {(accessRights.includes("role.create") || accessRights.includes("admin")) && (
                <Button
                    variant="contained"
                    onClick={handleOpenModal}
                    sx={{ mb: 3 }}
                >
                    Neue Rolle erstellen
                </Button>
            )}

            <RoleTable
                roles={roles}
                onEditRole={handleEditRole}
                onDeleteRole={handleDeleteRole}
            />

            <RoleFormModal
                open={isModalOpen}
                onClose={handleCloseModal}
                onSave={handleSaveRole}
                roleName={roleName}
                setRoleName={setRoleName}
                roleDescription={roleDescription}
                setRoleDescription={setRoleDescription}
                selectedPermissions={selectedPermissions}
                setSelectedPermissions={setSelectedPermissions}
                isEditing={!!editingRole}
                isDisabled={!roleName || selectedPermissions.length === 0}
            />

            {roleToDelete && (
                <DeleteConfirmationModal
                    open={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onConfirm={handleDeleteConfirm}
                    roleName={roleToDelete.roleName}
                />
            )}
        </Box>
    );
};

export default RoleCreation; 