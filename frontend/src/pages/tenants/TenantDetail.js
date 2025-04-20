import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  Box,
  Paper,
  Typography,
  Button,
  Grid,
  TextField,
  Alert,
  CircularProgress,
  Tabs,
  Tab,
  Card,
  CardContent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
} from '@mui/material';
import {
  ArrowBack,
  Refresh,
  Save,
  Delete,
  Person,
  Storage,
  Settings,
} from '@mui/icons-material';

// Custom TabPanel component
const TabPanel = (props) => {
  const { children, value, index, ...other } = props;
  
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tenant-tabpanel-${index}`}
      aria-labelledby={`tenant-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
};

// Format date helper
const formatDate = (dateString) => {
  const options = { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

const TenantDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  // Tab state
  const [tabValue, setTabValue] = useState(0);
  
  // Tenant state
  const [tenant, setTenant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  
  // Form state
  const [tenantName, setTenantName] = useState('');
  const [saveSuccess, setSaveSuccess] = useState(false);
  const [saving, setSaving] = useState(false);
  
  // S3 Config state
  const [s3Config, setS3Config] = useState({
    bucketName: '',
    endpoint: '',
    region: '',
    accessKey: '',
    secretKey: '',
    pathStyleAccess: false
  });
  
  // Dialog state
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  
  // Fetch tenant details
  useEffect(() => {
    fetchTenantDetails();
  }, [id]);
  
  const fetchTenantDetails = async () => {
    setLoading(true);
    setError('');
    setSaveSuccess(false);
    
    try {
      const response = await axios.get(`/api/tenants/${id}`);
      setTenant(response.data);
      setTenantName(response.data.name);
      
      // Fetch S3 bucket config
      const s3Response = await axios.get(`/api/tenants/${id}/buckets`);
      if (s3Response.data && s3Response.data.length > 0) {
        const defaultConfig = s3Response.data.find(config => config.isDefault === true) || s3Response.data[0];
        setS3Config({
          bucketName: defaultConfig.bucketName || '',
          endpoint: defaultConfig.endpoint || '',
          region: defaultConfig.region || '',
          accessKey: defaultConfig.accessKey || '',
          secretKey: defaultConfig.secretKey || '',
          pathStyleAccess: defaultConfig.pathStyleAccess || false
        });
      }
    } catch (err) {
      console.error('Error fetching tenant details:', err);
      setError(err.response?.data?.message || 'Failed to load tenant details. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };
  
  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };
  
  // Handle refresh
  const handleRefresh = () => {
    setRefreshing(true);
    fetchTenantDetails();
  };
  
  // Handle save tenant
  const handleSaveTenant = async () => {
    setSaving(true);
    setSaveSuccess(false);
    setError('');
    
    try {
      const tenantData = {
        name: tenantName,
        s3BucketName: s3Config.bucketName,
        s3Endpoint: s3Config.endpoint,
        s3Region: s3Config.region,
        s3AccessKey: s3Config.accessKey,
        s3SecretKey: s3Config.secretKey,
        s3PathStyleAccess: s3Config.pathStyleAccess
      };
      
      await axios.put(`/api/tenants/${id}`, tenantData);
      setSaveSuccess(true);
      fetchTenantDetails();
    } catch (err) {
      console.error('Error saving tenant:', err);
      setError(err.response?.data?.message || 'Failed to save tenant. Please try again.');
    } finally {
      setSaving(false);
    }
  };
  
  // Handle delete tenant
  const handleDeleteTenant = async () => {
    try {
      await axios.delete(`/api/tenants/${id}`);
      setShowDeleteDialog(false);
      navigate('/tenants');
    } catch (err) {
      console.error('Error deleting tenant:', err);
      setError(err.response?.data?.message || 'Failed to delete tenant. Please try again.');
      setShowDeleteDialog(false);
    }
  };
  
  // Handle S3 config changes
  const handleS3ConfigChange = (field) => (event) => {
    const value = field === 'pathStyleAccess' ? event.target.checked : event.target.value;
    setS3Config({
      ...s3Config,
      [field]: value
    });
  };
  
  // Render loading state
  if (loading) {
    return (
      <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <CircularProgress sx={{ mb: 2 }} />
        <Typography variant="body1">Loading tenant details...</Typography>
      </Box>
    );
  }
  
  // Render error state
  if (error && !tenant) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/tenants')}>
          Back to Tenants
        </Button>
      </Box>
    );
  }
  
  // Render tenant not found
  if (!tenant) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Tenant not found.
        </Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/tenants')}>
          Back to Tenants
        </Button>
      </Box>
    );
  }
  
  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <IconButton 
            onClick={() => navigate('/tenants')} 
            sx={{ mr: 1 }}
            aria-label="Back to tenants"
          >
            <ArrowBack />
          </IconButton>
          <Typography variant="h5" component="h1">
            Tenant Details
          </Typography>
        </Box>
        <Box>
          <Button 
            startIcon={<Refresh />} 
            onClick={handleRefresh}
            disabled={refreshing}
            sx={{ mr: 1 }}
          >
            Refresh
          </Button>
          <Button 
            startIcon={<Delete />} 
            color="error" 
            onClick={() => setShowDeleteDialog(true)}
          >
            Delete
          </Button>
        </Box>
      </Box>
      
      {/* Success/Error alerts */}
      {saveSuccess && (
        <Alert severity="success" sx={{ mb: 3 }}>
          Tenant updated successfully.
        </Alert>
      )}
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {/* Tenant summary card */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Typography variant="h6" gutterBottom>
              {tenant.name}
            </Typography>
            
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Tenant ID
                </Typography>
                <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
                  {tenant.id}
                </Typography>
              </Grid>
              
              <Grid item xs={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Subdomain
                </Typography>
                <Typography variant="body2">
                  {tenant.subdomain}
                </Typography>
              </Grid>
              
              <Grid item xs={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Created At
                </Typography>
                <Typography variant="body2">
                  {formatDate(tenant.createdAt)}
                </Typography>
              </Grid>
              
              <Grid item xs={6}>
                <Typography variant="subtitle2" color="text.secondary">
                  Status
                </Typography>
                <Typography variant="body2">
                  {tenant.active ? 'Active' : 'Inactive'}
                </Typography>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Tenant detail tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab icon={<Settings />} label="General" iconPosition="start" />
          <Tab icon={<Storage />} label="S3 Configuration" iconPosition="start" />
          <Tab icon={<Person />} label="Users" iconPosition="start" />
        </Tabs>
        
        {/* General Tab */}
        <TabPanel value={tabValue} index={0}>
          <Typography variant="h6" gutterBottom>
            General Settings
          </Typography>
          
          <Card variant="outlined">
            <CardContent>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Tenant Name"
                    fullWidth
                    value={tenantName}
                    onChange={(e) => setTenantName(e.target.value)}
                    margin="normal"
                  />
                  
                  <TextField
                    label="Subdomain"
                    fullWidth
                    value={tenant.subdomain}
                    disabled
                    margin="normal"
                    helperText="Subdomain cannot be changed after creation"
                  />
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                <Button 
                  variant="contained" 
                  startIcon={<Save />}
                  onClick={handleSaveTenant}
                  disabled={saving || tenantName === tenant.name}
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
              </Box>
            </CardContent>
          </Card>
        </TabPanel>
        
        {/* S3 Configuration Tab */}
        <TabPanel value={tabValue} index={1}>
          <Typography variant="h6" gutterBottom>
            S3 Storage Configuration
          </Typography>
          
          <Card variant="outlined">
            <CardContent>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Bucket Name"
                    fullWidth
                    value={s3Config.bucketName}
                    onChange={handleS3ConfigChange('bucketName')}
                    margin="normal"
                  />
                  
                  <TextField
                    label="S3 Endpoint"
                    fullWidth
                    value={s3Config.endpoint}
                    onChange={handleS3ConfigChange('endpoint')}
                    margin="normal"
                    helperText="For AWS, leave empty. For MinIO or custom S3, enter full URL"
                  />
                  
                  <TextField
                    label="Region"
                    fullWidth
                    value={s3Config.region}
                    onChange={handleS3ConfigChange('region')}
                    margin="normal"
                  />
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <TextField
                    label="Access Key"
                    fullWidth
                    value={s3Config.accessKey}
                    onChange={handleS3ConfigChange('accessKey')}
                    margin="normal"
                  />
                  
                  <TextField
                    label="Secret Key"
                    fullWidth
                    type="password"
                    value={s3Config.secretKey}
                    onChange={handleS3ConfigChange('secretKey')}
                    margin="normal"
                  />
                </Grid>
              </Grid>
              
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                <Button 
                  variant="contained" 
                  startIcon={<Save />}
                  onClick={handleSaveTenant}
                  disabled={saving}
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
              </Box>
            </CardContent>
          </Card>
        </TabPanel>
        
        {/* Users Tab */}
        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>
            Tenant Users
          </Typography>
          
          <Alert severity="info" sx={{ mb: 3 }}>
            User management is not yet implemented in this version.
          </Alert>
        </TabPanel>
      </Paper>
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        aria-labelledby="delete-tenant-dialog-title"
      >
        <DialogTitle id="delete-tenant-dialog-title">
          Delete Tenant
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this tenant? This action cannot be undone and all associated data will be permanently removed.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowDeleteDialog(false)}>
            Cancel
          </Button>
          <Button onClick={handleDeleteTenant} color="error">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TenantDetail; 