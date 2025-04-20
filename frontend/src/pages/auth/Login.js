import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Button, 
  TextField, 
  Typography, 
  Paper, 
  Grid, 
  Link as MuiLink, 
  Alert, 
  CircularProgress 
} from '@mui/material';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../../context/AuthContext';

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // Get redirect path from location state or default to dashboard
  const from = location.state?.from?.pathname || '/dashboard';

  // Check for auth errors in sessionStorage (from redirect)
  useEffect(() => {
    const sessionError = sessionStorage.getItem('auth_error');
    if (sessionError) {
      setError(sessionError);
      sessionStorage.removeItem('auth_error');
    }
  }, []);

  const formik = useFormik({
    initialValues: {
      username: '',
      password: ''
    },
    validationSchema: Yup.object({
      username: Yup.string().required('Username is required'),
      password: Yup.string().required('Password is required')
    }),
    onSubmit: async (values) => {
      setLoading(true);
      setError('');
      
      try {
        console.log('Submitting login form:', values.username);
        const result = await login(values.username, values.password);
        console.log('Login result:', result);
        
        if (result.success) {
          console.log('Login successful, navigating to:', from);
          navigate(from, { replace: true });
        } else {
          console.error('Login failed:', result.message);
          setError(result.message || 'Login failed. Please check your credentials.');
        }
      } catch (err) {
        console.error('Unexpected error during login:', err);
        if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError('Invalid username or password. Please try again.');
        }
      } finally {
        setLoading(false);
      }
    }
  });

  return (
    <Grid container justifyContent="center">
      <Grid item xs={12} sm={8} md={6} lg={4}>
        <Paper elevation={3} sx={{ p: 4, mt: 8 }}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            <Typography component="h1" variant="h5" gutterBottom>
              Sign in to DocLoader
            </Typography>
            
            {error && (
              <Alert 
                severity="error" 
                sx={{ width: '100%', mb: 2 }}
                variant="filled"
              >
                {error}
              </Alert>
            )}
            
            <Typography variant="caption" color="text.secondary" sx={{ mb: 2 }}>
              Using webpack proxy for API requests
            </Typography>
            
            <Box component="form" onSubmit={formik.handleSubmit} sx={{ mt: 1, width: '100%' }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Username"
                name="username"
                autoComplete="username"
                autoFocus
                value={formik.values.username}
                onChange={formik.handleChange}
                error={formik.touched.username && Boolean(formik.errors.username)}
                helperText={formik.touched.username && formik.errors.username}
                disabled={loading}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={formik.values.password}
                onChange={formik.handleChange}
                error={formik.touched.password && Boolean(formik.errors.password)}
                helperText={formik.touched.password && formik.errors.password}
                disabled={loading}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading}
              >
                {loading ? <CircularProgress size={24} /> : 'Sign In'}
              </Button>
              
              <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5 }}>
                <MuiLink component={Link} to="/forgot-password" variant="body2">
                  Forgot password?
                </MuiLink>
                
                <MuiLink component={Link} to="/register" variant="body2">
                  Don't have an account? Sign Up
                </MuiLink>
                
                <MuiLink component={Link} to="/register-tenant" variant="body2" sx={{ mt: 0.5 }}>
                  Register a new tenant organization
                </MuiLink>
              </Box>
            </Box>
          </Box>
        </Paper>
      </Grid>
    </Grid>
  );
};

export default Login; 