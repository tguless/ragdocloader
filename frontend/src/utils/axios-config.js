import axios from 'axios';

// Add a request interceptor that will add the token to all requests
axios.interceptors.request.use(
  config => {
    // Get token from localStorage
    const token = localStorage.getItem('token');
    
    // If token exists, add to headers
    if (token) {
      // Add Bearer prefix to token
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Add a response interceptor to handle auth errors
axios.interceptors.response.use(
  response => response,
  error => {
    // If the error is due to an expired token or unauthorized
    if (error.response && error.response.status === 401) {
      console.log('Authentication error - token may be expired or invalid');
      // Optionally redirect to login page
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axios; 