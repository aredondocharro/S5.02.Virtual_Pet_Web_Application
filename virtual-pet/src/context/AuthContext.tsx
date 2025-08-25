import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { apiGet } from '../api/client'; // 👈 usa tu cliente HTTP

export type AppUser = {
  id: number;
  username: string;
  email: string;
  avatarUrl?: string | null;
  roles?: string[];
};

type AuthContextType = {
  token: string | null;
  setToken: (t: string | null) => void;
  isAuth: boolean;
  user: AppUser | null;
  setUser: React.Dispatch<React.SetStateAction<AppUser | null>>;
  loadingUser: boolean;
  refreshUser: () => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType>({
  token: null, setToken: () => {}, isAuth: false,
  user: null, setUser: () => {}, loadingUser: false, refreshUser: async () => {}, logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem('token'));
  const [user, setUser] = useState<AppUser | null>(null);
  const [loadingUser, setLoadingUser] = useState(false);

  function setToken(t: string | null) {
    if (t) localStorage.setItem('token', t);
    else localStorage.removeItem('token');
    setTokenState(t);
  }

  async function refreshUser() {
    if (!token) { setUser(null); return; }
    try {
      setLoadingUser(true);
      // 👇 usa el mismo cliente/base que el resto de la app
      const me = await apiGet<AppUser>('/users/me');
      setUser(me);
    } catch (err) {
      console.error('Error loading /users/me', err);
      // Si quieres cerrar sesión ante 401, puedes hacerlo en apiGet o aquí.
    } finally {
      setLoadingUser(false);
    }
  }

  function logout() {
    setToken(null);
    setUser(null);
  }

  useEffect(() => { refreshUser(); /* eslint-disable-next-line */ }, [token]);

  const value = useMemo<AuthContextType>(() => ({
    token, setToken, isAuth: !!token,
    user, setUser, loadingUser, refreshUser, logout
  }), [token, user, loadingUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() { return useContext(AuthContext); }

