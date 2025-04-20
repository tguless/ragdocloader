import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isSystemAdmin, setIsSystemAdmin] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      fetchUserInfo();
    } else {
      setLoading(false);
    }
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await axios.get('/api/auth/me');
      setUser(response.data);
      setIsAuthenticated(true);
      
      // Check roles
      const roles = response.data.roles || [];
      setIsAdmin(roles.includes('ADMIN') || roles.includes('SYSTEM_ADMIN'));
      setIsSystemAdmin(roles.includes('SYSTEM_ADMIN'));
      
    } catch (err) {
      console.error('Error fetching user info:', err);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (username, password) => {
    try {
      const response = await axios.post('/api/auth/login', { username, password });
      const { token, ...userData } = response.data;
      
      localStorage.setItem('token', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setUser(userData);
      setIsAuthenticated(true);
      
      // Check roles
      const roles = userData.roles || [];
      setIsAdmin(roles.includes('ADMIN') || roles.includes('SYSTEM_ADMIN'));
      setIsSystemAdmin(roles.includes('SYSTEM_ADMIN'));
      
      return userData;
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Invalid credentials');
      throw err;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    delete axios.defaults.headers.common['Authorization'];
    setUser(null);
    setIsAuthenticated(false);
    setIsAdmin(false);
    setIsSystemAdmin(false);
  };

  const refreshUserInfo = () => {
    fetchUserInfo();
  };

  const value = {
    user,
    isAuthenticated,
    isAdmin,
    isSystemAdmin,
    loading,
    error,
    login,
    logout,
    refreshUserInfo
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}; 