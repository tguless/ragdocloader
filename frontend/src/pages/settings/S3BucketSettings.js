import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  FormControlLabel,
  Checkbox,
  Stack,
  Tooltip,
  CircularProgress,
  Alert,
  Snackbar,
  Chip
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Check,
  Star,
  StarBorder
} from '@mui/icons-material';
import axios from '../../utils/axios-config';
import { useAuth } from '../../context/AuthContext';

const S3BucketForm = ({ bucketConfig, onSubmit, onCancel, loading }) => {
  const [formData, setFormData] = useState({
    name: '',
    bucketName: '',
    endpoint: '',
    region: '',
    accessKey: '',
    secretKey: '',
    isDefault: false,
    pathStyleAccess: false,
    ...bucketConfig
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: e.target.type === 'checkbox' ? checked : value
    }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = 'Name is required';
    if (!formData.bucketName.trim()) newErrors.bucketName = 'Bucket name is required';
    if (!formData.accessKey.trim()) newErrors.accessKey = 'Access key is required';
    
    // Only require secretKey if this is a new bucket or if the user is changing it
    if (!bucketConfig?.id && !formData.secretKey.trim()) {
      newErrors.secretKey = 'Secret key is required';
    }
    
    // Region validation - allow empty for custom endpoints
    if (!formData.endpoint && !formData.region) {
      newErrors.region = 'Region is required when endpoint is not specified';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      // If editing and secretKey is empty, don't send it (to avoid overwriting)
      let dataToSubmit = {...formData};
      if (bucketConfig?.id && !dataToSubmit.secretKey) {
        delete dataToSubmit.secretKey;
      }
      onSubmit(dataToSubmit);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <Stack spacing={3}>
        <TextField
          fullWidth
          label="Configuration Name"
          name="name"
          value={formData.name}
          onChange={handleChange}
          error={!!errors.name}
          helperText={errors.name || "A friendly name for this configuration"}
          disabled={loading}
          required
        />
        
        <TextField
          fullWidth
          label="S3 Bucket Name"
          name="bucketName"
          value={formData.bucketName}
          onChange={handleChange}
          error={!!errors.bucketName}
          helperText={errors.bucketName || "The actual S3 bucket name"}
          disabled={loading}
          required
        />
        
        <TextField
          fullWidth
          label="Custom Endpoint (optional)"
          name="endpoint"
          value={formData.endpoint}
          onChange={handleChange}
          error={!!errors.endpoint}
          helperText={errors.endpoint || "For non-AWS S3 services like MinIO (e.g., http://minio:9000)"}
          disabled={loading}
        />
        
        <TextField
          fullWidth
          label="Region"
          name="region"
          value={formData.region}
          onChange={handleChange}
          error={!!errors.region}
          helperText={errors.region || "AWS region (e.g., us-east-1)"}
          disabled={loading}
        />
        
        <TextField
          fullWidth
          label="Access Key"
          name="accessKey"
          value={formData.accessKey}
          onChange={handleChange}
          error={!!errors.accessKey}
          helperText={errors.accessKey}
          disabled={loading}
          required
        />
        
        <TextField
          fullWidth
          label="Secret Key"
          name="secretKey"
          type="password"
          value={formData.secretKey}
          onChange={handleChange}
          error={!!errors.secretKey}
          helperText={errors.secretKey || (bucketConfig?.id ? "Leave empty to keep current secret key" : "")}
          disabled={loading}
          required={!bucketConfig?.id}
        />
        
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.isDefault}
                onChange={handleChange}
                name="isDefault"
                disabled={loading}
              />
            }
            label="Set as default bucket"
          />
          
          <FormControlLabel
            control={
              <Checkbox
                checked={formData.pathStyleAccess}
                onChange={handleChange}
                name="pathStyleAccess"
                disabled={loading}
              />
            }
            label="Use path-style access (required for MinIO)"
          />
        </Box>
      </Stack>
      
      <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button onClick={onCancel} disabled={loading}>
          Cancel
        </Button>
        <Button 
          type="submit" 
          variant="contained" 
          color="primary"
          disabled={loading}
          startIcon={loading ? <CircularProgress size={20} /> : null}
        >
          {bucketConfig?.id ? 'Update' : 'Create'} Bucket Config
        </Button>
      </Box>
    </form>
  );
};

const S3BucketSettings = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [bucketConfigs, setBucketConfigs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Dialog states
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogType, setDialogType] = useState(''); // 'add', 'edit', or 'delete'
  const [currentBucket, setCurrentBucket] = useState(null);
  
  const fetchBucketConfigs = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.get(`/api/tenants/${user.tenantId}/s3-configs`);
      setBucketConfigs(response.data);
    } catch (err) {
      console.error('Error fetching S3 bucket configs:', err);
      
      let errorMessage = 'Failed to load S3 bucket configurations. ';
      
      if (err.response?.status === 401) {
        errorMessage += 'Authentication failed. You may need to log in again.';
      } else if (err.response?.status === 403) {
        errorMessage += 'You do not have permission to access this resource.';
      } else if (err.response?.data?.message) {
        errorMessage += err.response.data.message;
      } else {
        errorMessage += 'Please try again.';
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    if (user && user.tenantId) {
      fetchBucketConfigs();
    } else {
      setError('User or tenant information is missing. Please log in again.');
    }
  }, [user?.tenantId]);
  
  const handleAddClick = () => {
    setDialogType('add');
    setCurrentBucket(null);
    setOpenDialog(true);
  };
  
  const handleEditClick = (bucket) => {
    setDialogType('edit');
    setCurrentBucket(bucket);
    setOpenDialog(true);
  };
  
  const handleDeleteClick = (bucket) => {
    setDialogType('delete');
    setCurrentBucket(bucket);
    setOpenDialog(true);
  };
  
  const handleCloseDialog = () => {
    setOpenDialog(false);
    setDialogType('');
    setCurrentBucket(null);
  };
  
  const handleFormSubmit = async (formData) => {
    setLoading(true);
    setError('');
    
    try {
      if (dialogType === 'add') {
        await axios.post(`/api/tenants/${user.tenantId}/s3-configs`, formData);
        setSuccess('S3 bucket configuration created successfully');
      } else if (dialogType === 'edit') {
        await axios.put(`/api/tenants/${user.tenantId}/s3-configs/${currentBucket.id}`, formData);
        setSuccess('S3 bucket configuration updated successfully');
      }
      
      fetchBucketConfigs();
      handleCloseDialog();
    } catch (err) {
      console.error('Error saving S3 bucket config:', err);
      setError(err.response?.data?.message || 'Failed to save S3 bucket configuration. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleDeleteConfirm = async () => {
    setLoading(true);
    setError('');
    
    try {
      await axios.delete(`/api/tenants/${user.tenantId}/s3-configs/${currentBucket.id}`);
      setSuccess('S3 bucket configuration deleted successfully');
      
      fetchBucketConfigs();
      handleCloseDialog();
    } catch (err) {
      console.error('Error deleting S3 bucket config:', err);
      setError(err.response?.data?.message || 'Failed to delete S3 bucket configuration. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleSnackbarClose = () => {
    setSuccess('');
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h1">
          S3 Bucket Configurations
        </Typography>
        
        <Box>
          <Button
            variant="outlined" 
            sx={{ mr: 2 }}
            onClick={() => fetchBucketConfigs()}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<Add />}
            onClick={handleAddClick}
          >
            Add S3 Bucket
          </Button>
        </Box>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        {loading && bucketConfigs.length === 0 ? (
          <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}>
            <CircularProgress />
          </Box>
        ) : bucketConfigs.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="body1" color="text.secondary">
              No S3 bucket configurations found.
            </Typography>
            <Button
              variant="outlined"
              color="primary"
              startIcon={<Add />}
              onClick={handleAddClick}
              sx={{ mt: 2 }}
            >
              Add Your First S3 Bucket
            </Button>
          </Box>
        ) : (
          <TableContainer sx={{ maxHeight: 440 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Bucket Name</TableCell>
                  <TableCell>Region</TableCell>
                  <TableCell>Endpoint</TableCell>
                  <TableCell align="center">Default</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {bucketConfigs.map((bucket) => (
                  <TableRow key={bucket.id}>
                    <TableCell component="th" scope="row">
                      {bucket.name}
                    </TableCell>
                    <TableCell>{bucket.bucketName}</TableCell>
                    <TableCell>{bucket.region || '-'}</TableCell>
                    <TableCell>{bucket.endpoint || '-'}</TableCell>
                    <TableCell align="center">
                      {bucket.isDefault ? (
                        <Chip 
                          icon={<Star fontSize="small" />} 
                          label="Default" 
                          color="primary" 
                          size="small" 
                        />
                      ) : null}
                    </TableCell>
                    <TableCell align="right">
                      <Tooltip title="Edit">
                        <IconButton onClick={() => handleEditClick(bucket)}>
                          <Edit fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete">
                        <IconButton 
                          onClick={() => handleDeleteClick(bucket)}
                          color="error"
                        >
                          <Delete fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>
      
      {/* Add/Edit Dialog */}
      <Dialog
        open={openDialog && (dialogType === 'add' || dialogType === 'edit')}
        onClose={handleCloseDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {dialogType === 'add' ? 'Add S3 Bucket Configuration' : 'Edit S3 Bucket Configuration'}
        </DialogTitle>
        <DialogContent>
          <S3BucketForm
            bucketConfig={currentBucket}
            onSubmit={handleFormSubmit}
            onCancel={handleCloseDialog}
            loading={loading}
          />
        </DialogContent>
      </Dialog>
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={openDialog && dialogType === 'delete'}
        onClose={handleCloseDialog}
      >
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the "{currentBucket?.name}" S3 bucket configuration?
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog} disabled={loading}>
            Cancel
          </Button>
          <Button 
            onClick={handleDeleteConfirm} 
            color="error"
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} /> : null}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
      
      {/* Success Snackbar */}
      <Snackbar
        open={!!success}
        autoHideDuration={6000}
        onClose={handleSnackbarClose}
        message={success}
        action={
          <IconButton
            size="small"
            color="inherit"
            onClick={handleSnackbarClose}
          >
            <Check />
          </IconButton>
        }
      />
    </Box>
  );
};

export default S3BucketSettings; 