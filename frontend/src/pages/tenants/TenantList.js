import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Alert,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';

// Helper function to format dates
const formatDate = (dateString) => {
  const options = { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric',
  };
  return new Date(dateString).toLocaleDateString(undefined, options);
};

const TenantList = () => {
  const navigate = useNavigate();
  const [tenants, setTenants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [tenantToDelete, setTenantToDelete] = useState(null);
  
  useEffect(() => {
    fetchTenants();
  }, []);
  
  const fetchTenants = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.get('/api/tenants');
      setTenants(response.data);
    } catch (err) {
      console.error('Error fetching tenants:', err);
      setError(err.response?.data?.message || 'Failed to load tenants. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };
  
  const handleRefresh = () => {
    setRefreshing(true);
    fetchTenants();
  };
  
  const handleCreateTenant = () => {
    navigate('/register-tenant');
  };
  
  const handleEditTenant = (id) => {
    navigate(`/tenants/${id}`);
  };
  
  const openDeleteDialog = (tenant) => {
    setTenantToDelete(tenant);
    setDeleteDialogOpen(true);
  };
  
  const closeDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setTenantToDelete(null);
  };
  
  const handleDeleteTenant = async () => {
    if (!tenantToDelete) return;
    
    try {
      await axios.delete(`/api/tenants/${tenantToDelete.id}`);
      fetchTenants();
      closeDeleteDialog();
    } catch (err) {
      console.error('Error deleting tenant:', err);
      setError(err.response?.data?.message || 'Failed to delete tenant. Please try again.');
      closeDeleteDialog();
    }
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h1">
          Tenant Management
        </Typography>
        <Box>
          <Button 
            startIcon={<RefreshIcon />} 
            onClick={handleRefresh}
            disabled={refreshing}
            sx={{ mr: 1 }}
          >
            Refresh
          </Button>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />} 
            onClick={handleCreateTenant}
          >
            Create Tenant
          </Button>
        </Box>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : tenants.length > 0 ? (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Subdomain</TableCell>
                <TableCell>Created At</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tenants.map((tenant) => (
                <TableRow key={tenant.id}>
                  <TableCell>{tenant.name}</TableCell>
                  <TableCell>{tenant.subdomain}</TableCell>
                  <TableCell>{formatDate(tenant.createdAt)}</TableCell>
                  <TableCell>
                    <Chip 
                      label={tenant.active ? 'Active' : 'Inactive'} 
                      color={tenant.active ? 'success' : 'default'} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell align="right">
                    <IconButton 
                      size="small" 
                      onClick={() => handleEditTenant(tenant.id)}
                      aria-label="Edit tenant"
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => openDeleteDialog(tenant)}
                      aria-label="Delete tenant"
                      color="error"
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <Alert severity="info">No tenants found. Create a new tenant to get started.</Alert>
      )}
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={closeDeleteDialog}
        aria-labelledby="delete-tenant-dialog-title"
      >
        <DialogTitle id="delete-tenant-dialog-title">
          Delete Tenant
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the tenant "{tenantToDelete?.name}"? 
            This action cannot be undone and will delete all data associated with this tenant.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDeleteDialog}>
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

export default TenantList; 