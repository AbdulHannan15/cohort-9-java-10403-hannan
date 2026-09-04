import React, { createContext, useContext, useEffect, useState } from 'react';
import { loginUser, registerUser, getCurrentUser } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('cms_token');
    if (!token) {
      setLoading(false);
      return;
    }
    getCurrentUser()
      .then((data) => setUser(data))
      .catch(() => {
        localStorage.removeItem('cms_token');
        localStorage.removeItem('cms_user');
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (loginIdentifier, password) => {
    const data = await loginUser({ loginIdentifier, password });
    localStorage.setItem('cms_token', data.token);
    const currentUser = {
      id: data.userId,
      loginIdentifier: data.loginIdentifier,
      role: data.role,
    };
    localStorage.setItem('cms_user', JSON.stringify(currentUser));
    setUser(currentUser);
    return currentUser;
  };

  const register = async (loginIdentifier, password, recoveryPhone) => {
    return registerUser({ loginIdentifier, password, recoveryPhone });
  };

  const logout = () => {
    localStorage.removeItem('cms_token');
    localStorage.removeItem('cms_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
