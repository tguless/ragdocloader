import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import {
  Box,
  Paper,
  Typography,
  Button,
  Switch,
  FormControlLabel,
  FormGroup,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider,
  Alert,
  CircularProgress,
  Grid,
  Card,
  CardContent,
} from '@mui/material';
import {
  Save,
  Refresh,
  Notifications,
  Brightness4,
  Language,
  Security,
  Storage,
  CloudUpload
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';

const Settings = () => {
  const { user } = useAuth();
  
  // Settings state
  const [settings, setSettings] = useState({
    theme: 'light',
    language: 'en',
    notifications: {
      email: true,
      browser: true,
      jobCompletion: true,
      jobFailure: true,
      systemUpdates: false,
    },
    security: {
      twoFactorAuth: false,
      sessionTimeout: 30,
    }
  });
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  
  // Load settings
  useEffect(() => {
    fetchSettings();
  }, []);
  
  const fetchSettings = async () => {
    setLoading(true);
    
    try {
      // In a real application, this would fetch from an API
      // Simulating API call for this example
      setTimeout(() => {
        // Mock settings data
        setSettings({
          theme: localStorage.getItem('theme') || 'light',
          language: localStorage.getItem('language') || 'en',
          notifications: {
            email: true,
            browser: true,
            jobCompletion: true,
            jobFailure: true,
            systemUpdates: false,
          },
          security: {
            twoFactorAuth: false,
            sessionTimeout: 30,
          }
        });
        setLoading(false);
      }, 500);
    } catch (err) {
      console.error('Error fetching settings:', err);
      setError('Failed to load settings. Please try again.');
      setLoading(false);
    }
  };
  
  // Handle theme change
  const handleThemeChange = (event) => {
    setSettings({
      ...settings,
      theme: event.target.checked ? 'dark' : 'light'
    });
  };
  
  // Handle language change
  const handleLanguageChange = (event) => {
    setSettings({
      ...settings,
      language: event.target.value
    });
  };
  
  // Handle notification settings change
  const handleNotificationChange = (setting) => (event) => {
    setSettings({
      ...settings,
      notifications: {
        ...settings.notifications,
        [setting]: event.target.checked
      }
    });
  };
  
  // Handle security settings change
  const handleSecurityChange = (setting, isNumeric = false) => (event) => {
    const value = isNumeric ? parseInt(event.target.value, 10) : event.target.checked;
    
    setSettings({
      ...settings,
      security: {
        ...settings.security,
        [setting]: value
      }
    });
  };
  
  // Save settings
  const handleSaveSettings = async () => {
    setSaving(true);
    setSuccess(false);
    setError('');
    
    try {
      // In a real application, this would save to an API
      // Simulating API call for this example
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // Save theme and language to localStorage for demo purposes
      localStorage.setItem('theme', settings.theme);
      localStorage.setItem('language', settings.language);
      
      setSuccess(true);
    } catch (err) {
      console.error('Error saving settings:', err);
      setError('Failed to save settings. Please try again.');
    } finally {
      setSaving(false);
    }
  };
  
  if (loading) {
    return (
      <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <CircularProgress sx={{ mb: 2 }} />
        <Typography variant="body1">Loading settings...</Typography>
      </Box>
    );
  }
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h1">
          Application Settings
        </Typography>
        <Box>
          <Button 
            startIcon={<Refresh />} 
            onClick={fetchSettings}
            sx={{ mr: 1 }}
          >
            Reset
          </Button>
          <Button 
            variant="contained" 
            startIcon={<Save />}
            onClick={handleSaveSettings}
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Settings'}
          </Button>
        </Box>
      </Box>
      
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Settings saved successfully.
        </Alert>
      )}
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {/* S3 Bucket Settings Link */}
      <Paper sx={{ p: 3, mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <CloudUpload sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
          <Box>
            <Typography variant="h6">S3 Bucket Settings</Typography>
            <Typography variant="body2" color="text.secondary">
              Configure S3 buckets for document storage and processing
            </Typography>
          </Box>
        </Box>
        <Button 
          variant="contained" 
          color="primary"
          component={Link}
          to="/settings/s3-buckets"
          startIcon={<Storage />}
        >
          Manage S3 Buckets
        </Button>
      </Paper>
      
      <Grid container spacing={3}>
        {/* Appearance Settings */}
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Brightness4 sx={{ mr: 1 }} />
                <Typography variant="h6">
                  Appearance
                </Typography>
              </Box>
              
              <Divider sx={{ mb: 2 }} />
              
              <FormGroup>
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.theme === 'dark'} 
                      onChange={handleThemeChange}
                    />
                  }
                  label="Dark Mode"
                />
              </FormGroup>
              
              <Box sx={{ mt: 2 }}>
                <FormControl fullWidth>
                  <InputLabel id="language-select-label">Language</InputLabel>
                  <Select
                    labelId="language-select-label"
                    value={settings.language}
                    label="Language"
                    onChange={handleLanguageChange}
                  >
                    <MenuItem value="en">English</MenuItem>
                    <MenuItem value="es">Spanish</MenuItem>
                    <MenuItem value="fr">French</MenuItem>
                    <MenuItem value="de">German</MenuItem>
                    <MenuItem value="zh">Chinese</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Notification Settings */}
        <Grid item xs={12} md={6}>
          <Card variant="outlined">
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Notifications sx={{ mr: 1 }} />
                <Typography variant="h6">
                  Notifications
                </Typography>
              </Box>
              
              <Divider sx={{ mb: 2 }} />
              
              <FormGroup>
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.notifications.email} 
                      onChange={handleNotificationChange('email')}
                    />
                  }
                  label="Email Notifications"
                />
                
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.notifications.browser} 
                      onChange={handleNotificationChange('browser')}
                    />
                  }
                  label="Browser Notifications"
                />
                
                <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
                  Notify me about:
                </Typography>
                
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.notifications.jobCompletion} 
                      onChange={handleNotificationChange('jobCompletion')}
                    />
                  }
                  label="Job Completions"
                />
                
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.notifications.jobFailure} 
                      onChange={handleNotificationChange('jobFailure')}
                    />
                  }
                  label="Job Failures"
                />
                
                <FormControlLabel
                  control={
                    <Switch 
                      checked={settings.notifications.systemUpdates} 
                      onChange={handleNotificationChange('systemUpdates')}
                    />
                  }
                  label="System Updates"
                />
              </FormGroup>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Security Settings */}
        <Grid item xs={12}>
          <Card variant="outlined">
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Security sx={{ mr: 1 }} />
                <Typography variant="h6">
                  Security
                </Typography>
              </Box>
              
              <Divider sx={{ mb: 2 }} />
              
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <FormGroup>
                    <FormControlLabel
                      control={
                        <Switch 
                          checked={settings.security.twoFactorAuth} 
                          onChange={handleSecurityChange('twoFactorAuth')}
                        />
                      }
                      label="Two-Factor Authentication"
                    />
                  </FormGroup>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel id="session-timeout-label">Session Timeout</InputLabel>
                    <Select
                      labelId="session-timeout-label"
                      value={settings.security.sessionTimeout}
                      label="Session Timeout"
                      onChange={handleSecurityChange('sessionTimeout', true)}
                    >
                      <MenuItem value={15}>15 minutes</MenuItem>
                      <MenuItem value={30}>30 minutes</MenuItem>
                      <MenuItem value={60}>1 hour</MenuItem>
                      <MenuItem value={120}>2 hours</MenuItem>
                      <MenuItem value={240}>4 hours</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Settings; 