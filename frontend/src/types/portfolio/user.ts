export type UserRole = 'USER' | 'PREMIUM_ADVISOR';

export interface User {
  id: string;        // UUID — Keycloak subject
  email: string;
  role: UserRole;
  createdAt: string; // ISO 8601
  updatedAt?: string;
}

export function isUser(obj: any): obj is User {
  return obj !== null && typeof obj === 'object' &&
    typeof obj.id === 'string' &&
    typeof obj.email === 'string' &&
    ['USER', 'PREMIUM_ADVISOR'].includes(obj.role) &&
    typeof obj.createdAt === 'string';
}
