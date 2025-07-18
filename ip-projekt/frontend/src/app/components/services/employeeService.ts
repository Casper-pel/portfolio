import { API_BASE_URL } from "../requests/baseUrl";

export interface Employee {
    employeeId: number;
    firstName: string;
    lastName: string;
    role?: {
        roleId: number;
        roleName: string;
        rolePermissions: string[];
    };
}

export const EmployeeService = {
    async getEmployeeById(employeeId: number): Promise<Employee | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/get/${employeeId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const employeeData = await response.json();
                return employeeData;
            } else if (response.status === 204) {
                console.warn(`Employee with ID ${employeeId} not found`);
                return null;
            } else {
                console.error('Failed to get employee:', response.statusText);
                return null;
            }
        } catch (error) {
            console.error('Error getting employee:', error);
            return null;
        }
    },

    async getAllEmployees(): Promise<Employee[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/all`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const employees = await response.json();
                return employees;
            } else {
                console.error('Failed to get all employees:', response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error getting all employees:', error);
            return [];
        }
    },

    formatEmployeeName(employee: Employee | null): string {
        if (!employee) {
            return 'Unbekannt';
        }
        return `${employee.firstName} ${employee.lastName}`;
    },

    async updateEmployee(employeeData: any): Promise<Employee | null> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/update`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(employeeData)
            });

            if (response.ok) {
                const message = await response.text();
                console.log('Employee update success:', message);

                return employeeData as Employee;
            } else {
                const errorMessage = await response.text();
                console.error('Failed to update employee:', errorMessage);
                return null;
            }
        } catch (error) {
            console.error('Error updating employee:', error);
            return null;
        }
    },

    async deleteEmployee(employeeId: number): Promise<boolean> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/delete/${employeeId}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                return true;
            } else if (response.status === 403) {
                throw new Error('Forbidden: Cannot delete this employee');
            } else {
                console.error('Failed to delete employee:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error deleting employee:', error);
            throw error;
        }
    },

    async updatePassword(oldPassword: string, newPassword: string): Promise<void> {
        try {
            const response = await fetch(`${API_BASE_URL}/employee/update-password`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    oldPassword,
                    newPassword
                })
            });

            if (response.ok) {
                console.log('Password updated successfully');
            } else {
                const errorMessage = await response.text();
                throw new Error(errorMessage || 'Failed to update password');
            }
        } catch (error) {
            console.error('Error updating password:', error);
            throw error;
        }
    }
};
