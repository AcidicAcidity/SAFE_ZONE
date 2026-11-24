export type Role = "CLIENT" | "ADMIN";
export type UserStatus = "ACTIVE" | "BLOCKED" | "DELETED";
export type CellSize = "SMALL" | "MEDIUM" | "LARGE";
export type CellStatus = "AVAILABLE" | "RENTED" | "UNAVAILABLE";
export type RentalStatus = "ACTIVE" | "EXPIRED" | "CANCELLED";

export interface User {
  id: number;
  login: string;
  role: Role;
  status: UserStatus;
}
export interface Warehouse {
  id: number;
  name: string;
  address: string;
}
export interface Cell {
  id: number;
  warehouseId: number;
  row: number;
  column: number;
  number: string;
  size: CellSize;
  status: CellStatus;
}
export interface Rental {
  id: number;
  userId?: number;
  cellId: number;
  startDateTime: string;
  endDateTime: string;
  status: RentalStatus;
}
export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}
export interface RentalRequest {
  cellId?: number;
  size?: CellSize;
  startDateTime: string;
  hours: number;
}

export const roleLabels: Record<Role, string> = {
  ADMIN: "Администратор",
  CLIENT: "Клиент",
};
export const sizeLabels: Record<CellSize, string> = {
  SMALL: "Малая",
  MEDIUM: "Средняя",
  LARGE: "Большая",
};
export const userStatusLabels: Record<UserStatus, string> = {
  ACTIVE: "Активен",
  BLOCKED: "Заблокирован",
  DELETED: "Удалён",
};
export const cellStatusLabels: Record<CellStatus, string> = {
  AVAILABLE: "Свободна",
  RENTED: "Занята",
  UNAVAILABLE: "Недоступна",
};
export const rentalStatusLabels: Record<RentalStatus, string> = {
  ACTIVE: "Действует",
  EXPIRED: "Завершена",
  CANCELLED: "Отменена",
};

export function countCellRentals(cells: Cell[], rentals: Rental[]) {
  const counts = new Map<number, number>();
  for (const rental of rentals)
    counts.set(rental.cellId, (counts.get(rental.cellId) || 0) + 1);
  return cells.map((cell) => ({
    ...cell,
    rentalCount: counts.get(cell.id) || 0,
  }));
}
