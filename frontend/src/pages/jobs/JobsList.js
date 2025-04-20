import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Chip,
  IconButton,
  Button,
  TextField,
  InputAdornment,
  MenuItem,
  FormControl,
  Select,
  InputLabel,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  CircularProgress,
  Alert,
  Tooltip,
} from '@mui/material';
import {
  Add,
  Refresh,
  Search,
  MoreVert,
  Delete,
  Visibility,
  FilterList,
  GetApp,
  Cancel,
  PlayArrow,
  Clear
} from '@mui/icons-material';
import axios from 'axios';

const JobsList = () => {
  const navigate = useNavigate();
  
  // State
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [orderBy, setOrderBy] = useState('createdAt');
  const [order, setOrder] = useState('desc');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [jobToDelete, setJobToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  
  // Status options for filtering
  const statusOptions = [
    { value: 'all', label: 'All Statuses' },
    { value: 'queued', label: 'Queued' },
    { value: 'processing', label: 'Processing' },
    { value: 'completed', label: 'Completed' },
    { value: 'failed', label: 'Failed' },
    { value: 'cancelled', label: 'Cancelled' },
    { value: 'paused', label: 'Paused' }
  ];
  
  // Colors for status chips
  const statusColors = {
    queued: 'info',
    processing: 'warning',
    completed: 'success',
    failed: 'error',
    cancelled: 'default',
    paused: 'secondary'
  };
  
  // Headers for table
  const headCells = [
    { id: 'id', label: 'ID' },
    { id: 'name', label: 'Job Name' },
    { id: 'type', label: 'Type' },
    { id: 'status', label: 'Status' },
    { id: 'createdAt', label: 'Created' },
    { id: 'documents', label: 'Documents' },
    { id: 'actions', label: 'Actions', disableSorting: true }
  ];
  
  // Fetch jobs
  const fetchJobs = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await axios.get('/api/jobs', {
        params: {
          page,
          limit: rowsPerPage,
          sort: orderBy,
          order,
          search: searchTerm,
          status: statusFilter !== 'all' ? statusFilter : undefined
        }
      });
      
      setJobs(response.data.jobs || []);
    } catch (err) {
      console.error('Error fetching jobs:', err);
      setError(err.response?.data?.message || 'Failed to load jobs');
    } finally {
      setLoading(false);
    }
  };
  
  // Load jobs on component mount and when filters/pagination change
  useEffect(() => {
    fetchJobs();
  }, [page, rowsPerPage, orderBy, order, statusFilter]);
  
  // Format date for display
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    
    const date = new Date(dateString);
    return date.toLocaleString();
  };
  
  // Handle searching
  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
  };
  
  const handleSearchSubmit = (event) => {
    event.preventDefault();
    setPage(0); // Reset to first page when searching
    fetchJobs();
  };
  
  // Handle sorting
  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };
  
  // Handle pagination
  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };
  
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };
  
  // Handle status filter change
  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
    setPage(0); // Reset to first page when filter changes
  };
  
  // Navigation to job detail
  const handleViewJob = (jobId) => {
    navigate(`/jobs/${jobId}`);
  };
  
  // Navigation to create job
  const handleCreateJob = () => {
    navigate('/jobs/create');
  };
  
  // Handle job deletion
  const handleDeleteClick = (job) => {
    setJobToDelete(job);
    setDeleteDialogOpen(true);
  };
  
  const handleDeleteConfirm = async () => {
    if (!jobToDelete) return;
    
    setDeleteLoading(true);
    
    try {
      await axios.delete(`/api/jobs/${jobToDelete.id}`);
      setDeleteDialogOpen(false);
      setJobToDelete(null);
      fetchJobs(); // Refresh the list after deletion
    } catch (err) {
      console.error('Error deleting job:', err);
      setError(err.response?.data?.message || 'Failed to delete job');
    } finally {
      setDeleteLoading(false);
    }
  };
  
  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setJobToDelete(null);
  };
  
  // Handle job actions (cancel, restart)
  const handleJobAction = async (jobId, action) => {
    try {
      await axios.post(`/api/jobs/${jobId}/${action}`);
      fetchJobs(); // Refresh the list
    } catch (err) {
      console.error(`Error performing ${action} action:`, err);
      setError(err.response?.data?.message || `Failed to ${action} job`);
    }
  };
  
  // Apply search filter locally
  const filteredJobs = searchTerm.length > 0 
    ? jobs.filter(job => 
        job.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        job.id.toString().includes(searchTerm) ||
        (job.type && job.type.toLowerCase().includes(searchTerm.toLowerCase()))
      )
    : jobs;
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h1">
          Document Processing Jobs
        </Typography>
        
        <Box>
          <Button 
            variant="contained" 
            color="primary" 
            startIcon={<Add />}
            onClick={handleCreateJob}
            sx={{ mr: 1 }}
          >
            New Job
          </Button>
          
          <Button 
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchJobs}
            disabled={loading}
          >
            Refresh
          </Button>
        </Box>
      </Box>
      
      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
      
      <Paper sx={{ mb: 3, p: 2 }}>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
          <form onSubmit={handleSearchSubmit} style={{ flexGrow: 1 }}>
            <TextField
              fullWidth
              label="Search jobs"
              variant="outlined"
              size="small"
              value={searchTerm}
              onChange={handleSearch}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Search />
                  </InputAdornment>
                ),
                endAdornment: searchTerm && (
                  <InputAdornment position="end">
                    <IconButton
                      size="small"
                      onClick={() => {
                        setSearchTerm('');
                        if (searchTerm) fetchJobs();
                      }}
                    >
                      <Clear />
                    </IconButton>
                  </InputAdornment>
                )
              }}
            />
          </form>
          
          <FormControl sx={{ minWidth: 200 }} size="small">
            <InputLabel id="status-filter-label">Status</InputLabel>
            <Select
              labelId="status-filter-label"
              id="status-filter"
              value={statusFilter}
              label="Status"
              onChange={handleStatusFilterChange}
              startAdornment={
                <InputAdornment position="start">
                  <FilterList fontSize="small" />
                </InputAdornment>
              }
            >
              {statusOptions.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Paper>
      
      <Paper sx={{ width: '100%', overflow: 'hidden' }}>
        <TableContainer sx={{ maxHeight: 600 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                {headCells.map((headCell) => (
                  <TableCell 
                    key={headCell.id}
                    sortDirection={orderBy === headCell.id ? order : false}
                  >
                    {!headCell.disableSorting ? (
                      <TableSortLabel
                        active={orderBy === headCell.id}
                        direction={orderBy === headCell.id ? order : 'asc'}
                        onClick={() => handleRequestSort(headCell.id)}
                      >
                        {headCell.label}
                      </TableSortLabel>
                    ) : (
                      headCell.label
                    )}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={headCells.length} align="center" sx={{ py: 3 }}>
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : filteredJobs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={headCells.length} align="center" sx={{ py: 3 }}>
                    <Typography variant="body1" color="text.secondary">
                      No jobs found
                    </Typography>
                    {searchTerm && (
                      <Button 
                        sx={{ mt: 1 }}
                        onClick={() => {
                          setSearchTerm('');
                          fetchJobs();
                        }}
                      >
                        Clear Search
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ) : (
                filteredJobs.map((job) => (
                  <TableRow hover key={job.id}>
                    <TableCell>{job.id}</TableCell>
                    <TableCell>
                      <Typography 
                        variant="body2" 
                        sx={{ 
                          fontWeight: 'medium',
                          cursor: 'pointer',
                          '&:hover': { textDecoration: 'underline' }
                        }}
                        onClick={() => handleViewJob(job.id)}
                      >
                        {job.name}
                      </Typography>
                      {job.description && (
                        <Typography variant="caption" color="text.secondary" noWrap>
                          {job.description}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      {job.type && (
                        <Typography variant="body2">
                          {job.type.replace(/_/g, ' ')}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={job.status}
                        size="small"
                        color={statusColors[job.status] || 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      <Tooltip title={formatDate(job.createdAt)}>
                        <Typography variant="body2">
                          {formatDate(job.createdAt).split(',')[0]}
                        </Typography>
                      </Tooltip>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={job.documentCount || '0'} 
                        size="small" 
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex' }}>
                        <Tooltip title="View Details">
                          <IconButton size="small" onClick={() => handleViewJob(job.id)}>
                            <Visibility fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        
                        {['queued', 'processing'].includes(job.status) && (
                          <Tooltip title="Cancel Job">
                            <IconButton 
                              size="small" 
                              color="error"
                              onClick={() => handleJobAction(job.id, 'cancel')}
                            >
                              <Cancel fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                        
                        {job.status === 'paused' && (
                          <Tooltip title="Resume Job">
                            <IconButton 
                              size="small" 
                              color="primary"
                              onClick={() => handleJobAction(job.id, 'resume')}
                            >
                              <PlayArrow fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                        
                        {['completed', 'failed'].includes(job.status) && (
                          <Tooltip title="Download Results">
                            <IconButton 
                              size="small"
                              disabled={!job.resultsUrl} 
                              href={job.resultsUrl} 
                              target="_blank"
                            >
                              <GetApp fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                        
                        <Tooltip title="Delete Job">
                          <IconButton 
                            size="small" 
                            color="error"
                            onClick={() => handleDeleteClick(job)}
                          >
                            <Delete fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={jobs.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>
      
      {/* Delete confirmation dialog */}
      <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
        <DialogTitle>Delete Job</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete job "{jobToDelete?.name}"? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDeleteCancel} disabled={deleteLoading}>
            Cancel
          </Button>
          <Button 
            onClick={handleDeleteConfirm} 
            color="error" 
            disabled={deleteLoading}
            startIcon={deleteLoading ? <CircularProgress size={20} /> : null}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default JobsList; 