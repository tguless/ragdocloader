import React from 'react';
import { Box, Container } from '@mui/material';
import Navbar from './Navbar';
import { useAuth } from '../../context/AuthContext';
import { Navigate } from 'react-router-dom';

const MainLayout = ({ children, requireAuth = true }) => {
  const { isAuthenticated, loading } = useAuth();

  // Show loading indicator or redirect if authentication is required but not present
  if (requireAuth && !loading && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      <Container component="main" sx={{ flexGrow: 1, py: 3 }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
            <div>Loading...</div>
          </Box>
        ) : children}
      </Container>
      <Box
        component="footer"
        sx={{
          py: 3,
          px: 2,
          mt: 'auto',
          backgroundColor: (theme) => theme.palette.grey[100],
          textAlign: 'center'
        }}
      >
        <Container maxWidth="sm">
          DocLoader &copy; {new Date().getFullYear()}
        </Container>
      </Box>
    </Box>
  );
};

export default MainLayout; 