interface Session {
  token: string;
  expiresAt: number;
}
const key = "safezone.session.v1";
type Listener = (reason?: "expired") => void;
const listeners = new Set<Listener>();
let memory: Session | null = null;

try {
  const stored: Session = JSON.parse(sessionStorage.getItem(key) || "null");
  if (
    stored?.token &&
    Number.isFinite(stored.expiresAt) &&
    stored.expiresAt > Date.now()
  )
    memory = stored;
  else sessionStorage.removeItem(key);
} catch {
  /* The app also works when browser storage is unavailable. */
}

export const session = {
  get: () => memory,
  subscribe(listener: Listener) {
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  },
  save(token: string, ttlSeconds: number) {
    memory = { token, expiresAt: Date.now() + ttlSeconds * 1000 };
    try {
      sessionStorage.setItem(key, JSON.stringify(memory));
    } catch {
      /* Use memory. */
    }
    listeners.forEach((listener) => listener());
  },
  clear(reason?: "expired") {
    memory = null;
    try {
      sessionStorage.removeItem(key);
    } catch {
      /* Use memory. */
    }
    listeners.forEach((listener) => listener(reason));
  },
};
