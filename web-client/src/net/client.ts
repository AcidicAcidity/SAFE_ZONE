import type {
  Cell,
  Rental,
  RentalRequest,
  Role,
  TokenResponse,
  User,
  UserStatus,
  Warehouse,
} from "../model/models";
import { session } from "./session";

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

type RequestOptions = RequestInit & {
  authenticated?: boolean;
  fallback?: string;
};

export async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { authenticated = true, fallback, ...init } = options;
  const current = session.get();
  if (authenticated && current && current.expiresAt <= Date.now()) {
    session.clear("expired");
    throw new ApiError(401, "Сессия истекла. Войдите в аккаунт снова.");
  }
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");
  if (init.body) headers.set("Content-Type", "application/json");
  if (authenticated && current)
    headers.set("Authorization", `Bearer ${current.token}`);
  let response: Response;
  try {
    const timeout = AbortSignal.timeout(15_000);
    const signal = init.signal
      ? AbortSignal.any([init.signal, timeout])
      : timeout;
    response = await fetch(`/api/v1${path}`, { ...init, headers, signal });
  } catch (error) {
    if (init.signal?.aborted) throw error;
    throw new ApiError(
      0,
      "Не удалось связаться с сервером. Проверьте соединение и повторите попытку.",
    );
  }
  if (!response.ok) {
    // A stale in-flight request must not end a newer login session.
    if (
      response.status === 401 &&
      authenticated &&
      current &&
      session.get()?.token === current.token
    )
      session.clear("expired");
    const payload: unknown = await response.json().catch(() => null);
    const message =
      payload && typeof payload === "object" && "message" in payload
        ? String(payload.message || "")
        : "";
    const safeMessage =
      message.length < 250 &&
      !/Exception|java\.|SQL|JDBC|Validation failed|HV\d+/i.test(message)
        ? message
        : "";
    const defaults: Record<number, string> = {
      401: "Сессия истекла. Войдите в аккаунт снова.",
      403: "Недостаточно прав для этого действия.",
      404: "Запрошенные данные не найдены.",
      409: "Данные изменились. Обновите страницу и попробуйте снова.",
    };
    throw new ApiError(
      response.status,
      (response.status < 500 && safeMessage) ||
        fallback ||
        defaults[response.status] ||
        "Не удалось выполнить запрос. Повторите попытку позже.",
    );
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

// Keep the backend's historical field names at this boundary.
export function normalizeUser(
  raw: Omit<User, "role"> & { role?: Role; Role?: Role },
): User {
  const role = raw.role || raw.Role;
  if (role !== "ADMIN" && role !== "CLIENT")
    throw new ApiError(502, "Не удалось определить права аккаунта.");
  return { id: raw.id, login: raw.login, status: raw.status, role };
}
export function normalizeRental(
  raw: Omit<Rental, "startDateTime" | "endDateTime"> & {
    startDateTime?: string;
    endDateTime?: string;
    startRent?: string;
    endRent?: string;
  },
): Rental {
  return {
    id: raw.id,
    userId: raw.userId,
    cellId: raw.cellId,
    status: raw.status,
    startDateTime: raw.startDateTime || raw.startRent || "",
    endDateTime: raw.endDateTime || raw.endRent || "",
  };
}

type RawUser = Parameters<typeof normalizeUser>[0];
type RawRental = Parameters<typeof normalizeRental>[0];
export const api = {
  login: (login: string, password: string) =>
    request<TokenResponse>("/auth/login", {
      method: "POST",
      authenticated: false,
      body: JSON.stringify({ login, password }),
      fallback:
        "Не удалось войти. Проверьте логин и пароль или повторите попытку позже.",
    }),
  register: (login: string, password: string) =>
    request<RawUser>("/auth/register", {
      method: "POST",
      authenticated: false,
      body: JSON.stringify({ login, password }),
      fallback:
        "Не удалось зарегистрироваться. Попробуйте другой логин или повторите попытку позже.",
    }),
  me: (signal?: AbortSignal) =>
    request<RawUser>("/auth/me", { signal }).then(normalizeUser),
  warehouses: (signal?: AbortSignal) =>
    request<
      (Omit<Warehouse, "address"> & { addr?: string; address?: string })[]
    >("/warehouses", { signal }).then((rows) =>
      rows.map((row) => ({
        id: row.id,
        name: row.name,
        address: row.address || row.addr || "—",
      })),
    ),
  cells: (signal?: AbortSignal) => request<Cell[]>("/cells", { signal }),
  availableCells: (signal?: AbortSignal) => {
    // This controller currently selects the no-argument service only when BOTH dates are provided.
    const start = new Date();
    const end = new Date(start.getTime() + 3_600_000);
    return request<Cell[]>(
      `/cells/available?start=${encodeURIComponent(localDateTime(start))}&end=${encodeURIComponent(localDateTime(end))}`,
      { signal },
    );
  },
  myRentals: (signal?: AbortSignal) =>
    request<RawRental[]>("/rentals/me", { signal }).then((rows) =>
      rows.map(normalizeRental),
    ),
  async rentals(signal?: AbortSignal) {
    try {
      return (await request<RawRental[]>("/rentals", { signal })).map(
        normalizeRental,
      );
    } catch (error) {
      // Spring combines the class path with the repeated method path in the current API.
      if (!(error instanceof ApiError) || ![404, 405].includes(error.status))
        throw error;
      return (
        await request<RawRental[]>("/rentals/api/v1/rentals", { signal })
      ).map(normalizeRental);
    }
  },
  users: (signal?: AbortSignal) =>
    request<RawUser[]>("/users", { signal }).then((rows) =>
      rows.map(normalizeUser),
    ),
  createRental: (body: RentalRequest) =>
    request<RawRental>("/rentals", {
      method: "POST",
      body: JSON.stringify(body),
    }).then(normalizeRental),
  cancelRental: (id: number) =>
    request<RawRental>(`/rentals/${id}/cancel`, { method: "PATCH" }).then(
      normalizeRental,
    ),
  cancelRentalAsAdmin: (id: number) =>
    request<RawRental>(`/rentals/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status: "CANCELLED" }),
    }).then(normalizeRental),
  changeUserStatus: (id: number, status: UserStatus) =>
    request<RawUser>(`/users/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }).then(normalizeUser),
};

export function localDateTime(date = new Date()): string {
  const part = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${part(date.getMonth() + 1)}-${part(date.getDate())}T${part(date.getHours())}:${part(date.getMinutes())}:${part(date.getSeconds())}`;
}
