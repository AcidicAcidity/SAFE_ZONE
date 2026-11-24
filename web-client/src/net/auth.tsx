import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import type { User } from "../model/models";
import { api } from "./client";
import { session } from "./session";

interface Auth {
  user?: User;
  loading: boolean;
  error: Error | null;
  notice: string;
  login: (login: string, password: string) => Promise<void>;
  logout: () => void;
  retry: () => void;
}
const AuthContext = createContext<Auth | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [current, setCurrent] = useState(session.get());
  const [notice, setNotice] = useState("");
  const me = useQuery({
    queryKey: ["identity", current?.token],
    queryFn: ({ signal }) => api.me(signal),
    enabled: !!current,
    retry: false,
    staleTime: 60_000,
  });
  useEffect(
    () =>
      session.subscribe((reason) => {
        setCurrent(session.get());
        setNotice(
          reason === "expired"
            ? "Сессия истекла. Войдите в аккаунт снова."
            : "",
        );
        queryClient.clear();
      }),
    [queryClient],
  );
  useEffect(() => {
    if (!current) return;
    const timer = window.setTimeout(
      () => session.clear("expired"),
      Math.max(0, current.expiresAt - Date.now()),
    );
    return () => window.clearTimeout(timer);
  }, [current]);
  useEffect(() => {
    if (me.data && me.data.status !== "ACTIVE") session.clear("expired");
  }, [me.data]);
  const value: Auth = {
    user: current && me.data?.status === "ACTIVE" ? me.data : undefined,
    loading: !!current && me.isPending,
    error: current ? me.error : null,
    notice,
    async login(login, password) {
      const result = await api.login(login, password);
      session.save(result.accessToken, result.expiresInSeconds);
    },
    logout: () => session.clear(),
    retry: () => {
      void me.refetch();
    },
  };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const auth = useContext(AuthContext);
  if (!auth) throw new Error("AuthProvider is missing");
  return auth;
}
