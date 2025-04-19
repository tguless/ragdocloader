import React, { createContext, useState, useEffect, useContext } from 'react';
import { jwtDecode } from 'jwt-decode';
import axios from 'axios';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        const currentTime = Date.now() / 1000;
        
        if (decoded.exp < currentTime) {
          // Token is expired
          logout();
        } else {
          // Set auth header for all future requests
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          // Fetch current user info
          axios.get('/api/auth/me')
            .then(response => {
              setCurrentUser(response.data);
              setLoading(false);
            })
            .catch(err => {
              console.error('Error fetching user data:', err);
              logout();
              setLoading(false);
            });
        }
      } catch (err) {
        console.error('Error decoding token:', err);
        logout();
        setLoading(false);
      }
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (username, password) => {
    try {
      setError(null);
      const response = await axios.post('/api/auth/login', { username, password });
      const { token, id, username: user, email, tenantId, roles } = response.data;
      
      localStorage.setItem('token', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setCurrentUser({ id, username: user, email, tenantId, roles });
      return { success: true };
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to login');
      return { success: false, message: err.response?.data?.message || 'Failed to login' };
    }
  };

  const register = async (userData) => {
    try {
      setError(null);
      const response = await axios.post('/api/auth/register', userData);
      const { token, id, username, email, tenantId, roles } = response.data;
      
      localStorage.setItem('token', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setCurrentUser({ id, username, email, tenantId, roles });
      return { success: true };
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to register');
      return { success: false, message: err.response?.data?.message || 'Failed to register' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    delete axios.defaults.headers.common['Authorization'];
    setCurrentUser(null);
  };

  const value = {
    currentUser,
    loading,
    error,
    login,
    register,
    logout,
    isAuthenticated: !!currentUser,
    isAdmin: currentUser?.roles?.includes('ADMIN') || false
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}; 