export interface Role {
    roleId: number | null;
    roleName: string;
    description: string;
    rolePermissions: string[];
    employees: string[];
}
