import { API_BASE_URL } from "../requests/baseUrl";

export interface Role {
    roleId: number | null;
    roleName: string;
    description: string;
    rolePermissions: string[];
}

export const RoleService = {
    async getAllRoles(): Promise<Role[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/role/all`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const roles = await response.json();
                return roles;
            } else if (response.status === 204) {
                return [];
            } else {
                console.error('Failed to get all roles:', response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error getting all roles:', error);
            return [];
        }
    },

    async getRoleById(roleId: number): Promise<Role | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/role/get/${roleId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const role = await response.json();
                return role;
            } else if (response.status === 204) {
                console.warn(`Role with ID ${roleId} not found`);
                return null;
            } else {
                console.error('Failed to get role:', response.statusText);
                return null;
            }
        } catch (error) {
            console.error('Error getting role:', error);
            return null;
        }
    },

    async createRole(roleData: Omit<Role, 'roleId'>): Promise<Role | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/role/add`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(roleData)
            });

            if (response.ok) {
                const message = await response.text();
                console.log('Role creation success:', message);

                return { ...roleData, roleId: Date.now() } as Role;
            } else {
                const errorMessage = await response.text();
                console.error('Failed to create role:', errorMessage);
                return null;
            }
        } catch (error) {
            console.error('Error creating role:', error);
            return null;
        }
    },

    async updateRole(roleData: Role): Promise<Role | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/role/update`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(roleData)
            });

            if (response.ok) {
                const message = await response.text();
                console.log('Role update success:', message);
                
                return roleData;
            } else {
                const errorMessage = await response.text();
                console.error('Failed to update role:', errorMessage);
                return null;
            }
        } catch (error) {
            console.error('Error updating role:', error);
            return null;
        }
    },

    async deleteRole(roleId: number): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/role/delete/${roleId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                return true;
            } else {
                console.error('Failed to delete role:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error deleting role:', error);
            return false;
        }
    }
};
