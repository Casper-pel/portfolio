import { Role } from "./role";

export interface UserWithoutPassword {
    userId: number;
    username: string;
    role: Role;
}