import api from './axiosConfig';

const authService = {
  // Login user
  login: async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      
      // Store token in localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        
        // If there's a tenant ID, store it too for multi-tenancy
        if (response.data.tenantId) {
          localStorage.setItem('tenantId', response.data.tenantId);
        }
      }
      
      return response.data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  // Register user
  register: async (registerData) => {
    try {
      const response = await api.post('/auth/register', registerData);
      
      // Store token in localStorage
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        
        // If there's a tenant ID, store it too for multi-tenancy
        if (response.data.tenantId) {
          localStorage.setItem('tenantId', response.data.tenantId);
        }
      }
      
      return response.data;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  // Get current user information
  getCurrentUser: async () => {
    try {
      const response = await api.get('/auth/me');
      return response.data;
    } catch (error) {
      console.error('Error fetching current user:', error);
      throw error;
    }
  },

  // Logout user
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('tenantId');
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

export default authService; 