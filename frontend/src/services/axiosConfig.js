import axios from 'axios';

// Create axios instance with default config
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for API calls
api.interceptors.request.use(
  (config) => {
    console.log('Request interceptor:', config.url);
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('Token applied to request');
    } else {
      console.log('No token found in localStorage');
    }
    
    // Add tenant header if needed (example for multi-tenant applications)
    const tenantHeader = localStorage.getItem('tenantId');
    if (tenantHeader) {
      config.headers['X-TenantID'] = tenantHeader;
    }
    
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for API calls
api.interceptors.response.use(
  (response) => {
    console.log('Response interceptor success:', response.config.url);
    return response;
  },
  async (error) => {
    console.error('Response interceptor error:', error.config?.url, error.response?.status);
    const originalRequest = error.config;
    
    // Don't redirect for auth-related endpoints - let components handle these errors
    if (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/register')) {
      return Promise.reject(error);
    }
    
    // Handle unauthorized errors (401)
    if (error.response?.status === 401 && !originalRequest._retry) {
      console.warn('Unauthorized access detected, redirecting to login');
      
      // Store the error message in sessionStorage so it can be displayed on the login page
      if (error.response?.data?.message) {
        sessionStorage.setItem('auth_error', error.response.data.message);
      } else {
        sessionStorage.setItem('auth_error', 'Your session has expired. Please log in again.');
      }
      
      // Clear token and redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default api; 