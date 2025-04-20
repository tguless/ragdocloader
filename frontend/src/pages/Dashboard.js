import React, { useEffect, useState } from 'react';
import { Box, Typography, Grid, Paper, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalJobs: 0,
    pendingJobs: 0,
    completedJobs: 0,
    failedJobs: 0,
    totalDocuments: 0
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/dashboard');
        setStats(response.data);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const handleCardClick = (path) => {
    navigate(path);
  };

  const DashboardCard = ({ title, value, color, onClick }) => (
    <Paper 
      elevation={3} 
      sx={{ 
        p: 3, 
        display: 'flex', 
        flexDirection: 'column', 
        height: 140, 
        bgcolor: color,
        color: 'white',
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? { transform: 'translateY(-4px)', transition: 'transform 0.3s' } : {}
      }}
      onClick={onClick}
    >
      <Typography variant="h5" component="h2" fontWeight="bold">
        {title}
      </Typography>
      <Typography variant="h3" component="p" sx={{ mt: 2, fontWeight: 'medium' }}>
        {value}
      </Typography>
    </Paper>
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Dashboard
      </Typography>
      
      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} sm={6} md={4}>
          <DashboardCard 
            title="Total Jobs" 
            value={stats.totalJobs} 
            color="#2196f3"
            onClick={() => handleCardClick('/jobs')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <DashboardCard 
            title="Pending Jobs" 
            value={stats.pendingJobs} 
            color="#ff9800"
            onClick={() => handleCardClick('/jobs?status=pending')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <DashboardCard 
            title="Completed Jobs" 
            value={stats.completedJobs} 
            color="#4caf50"
            onClick={() => handleCardClick('/jobs?status=completed')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <DashboardCard 
            title="Failed Jobs" 
            value={stats.failedJobs} 
            color="#f44336"
            onClick={() => handleCardClick('/jobs?status=failed')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <DashboardCard 
            title="Documents Processed" 
            value={stats.totalDocuments} 
            color="#9c27b0"
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard; 