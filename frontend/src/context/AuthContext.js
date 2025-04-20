import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/axiosConfig';

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
      // No need to set axios defaults here, the api instance handles this
      fetchUserInfo();
    } else {
      setLoading(false);
    }
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await api.get('/auth/me');
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
      console.log('Attempting login for user:', username);
      
      // Use the api instance which leverages the webpack proxy
      const response = await api.post('/auth/login', { 
        username, 
        password 
      });
      
      console.log('Login response:', response.data);
      
      const { token, ...userData } = response.data;
      
      if (!token) {
        console.error('No token received in response');
        return { success: false, message: 'Authentication failed: No token received' };
      }
      
      localStorage.setItem('token', token);
      // The api instance will automatically use this token for future requests
      
      setUser(userData);
      setIsAuthenticated(true);
      
      // Check roles
      const roles = userData.roles || [];
      setIsAdmin(roles.includes('ADMIN') || roles.includes('SYSTEM_ADMIN'));
      setIsSystemAdmin(roles.includes('SYSTEM_ADMIN'));
      
      return { success: true, user: userData };
    } catch (err) {
      console.error('Login error:', err);
      const errorMessage = err.response?.data?.message || 
                        (err.response?.status === 401 ? 'Invalid username or password' : 
                        'Unable to connect to authentication service');
      setError(errorMessage);
      return { success: false, message: errorMessage };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
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