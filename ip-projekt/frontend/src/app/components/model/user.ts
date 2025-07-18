import { Role } from "./role";

export interface User {
    employeeId: number;
    firstName: string;
    lastName: string;
    password: string;
    role: Role;
}