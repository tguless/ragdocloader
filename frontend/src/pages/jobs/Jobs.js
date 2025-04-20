import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  CircularProgress,
  IconButton,
  Alert,
  TextField,
  InputAdornment,
  Tooltip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from '@mui/material';
import {
  Add,
  Search,
  Refresh,
  Delete,
  Visibility,
  CloudUpload
} from '@mui/icons-material';
import api from '../../services/axiosConfig';

// Status color mapping
const statusColors = {
  PENDING: 'warning',
  PROCESSING: 'info',
  COMPLETED: 'success',
  FAILED: 'error'
};

const Jobs = () => {
  const navigate = useNavigate();
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalCount, setTotalCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [jobToDelete, setJobToDelete] = useState(null);

  // Fetch jobs data
  const fetchJobs = async () => {
    try {
      setLoading(true);
      setError('');
      
      const response = await api.get('/jobs', {
        params: {
          page: page + 1,
          limit: rowsPerPage,
          search: searchQuery.trim() || undefined
        }
      });
      
      setJobs(response.data.jobs || []);
      setTotalCount(response.data.totalCount || 0);
    } catch (err) {
      console.error('Error fetching jobs:', err);
      setError('Failed to load jobs. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // Refresh jobs data
  const handleRefresh = () => {
    setRefreshing(true);
    fetchJobs();
  };

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  };

  // Handle page change
  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  // Handle rows per page change
  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  // Handle search
  const handleSearchChange = (event) => {
    setSearchQuery(event.target.value);
  };

  const handleSearch = (event) => {
    if (event.key === 'Enter') {
      setPage(0);
      fetchJobs();
    }
  };

  // Navigate to job details
  const handleViewJob = (jobId) => {
    navigate(`/jobs/${jobId}`);
  };

  // Navigate to create job page
  const handleCreateJob = () => {
    navigate('/jobs/create');
  };

  // Delete job
  const handleDeleteJob = async () => {
    if (!jobToDelete) return;
    
    try {
      await api.delete(`/jobs/${jobToDelete}`);
      fetchJobs();
      setDeleteDialogOpen(false);
      setJobToDelete(null);
    } catch (err) {
      console.error('Error deleting job:', err);
      setError('Failed to delete job. Please try again.');
    }
  };

  // Open delete confirmation dialog
  const openDeleteDialog = (jobId, event) => {
    event.stopPropagation();
    setJobToDelete(jobId);
    setDeleteDialogOpen(true);
  };

  // Close delete confirmation dialog
  const closeDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setJobToDelete(null);
  };

  // Load data on component mount and when dependencies change
  useEffect(() => {
    fetchJobs();
  }, [page, rowsPerPage]);

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h1">
          Document Processing Jobs
        </Typography>
        <Box>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleCreateJob}
            sx={{ mr: 1 }}
          >
            New Job
          </Button>
          <Tooltip title="Refresh">
            <IconButton onClick={handleRefresh} disabled={refreshing}>
              {refreshing ? <CircularProgress size={24} /> : <Refresh />}
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Search jobs..."
          value={searchQuery}
          onChange={handleSearchChange}
          onKeyPress={handleSearch}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search />
              </InputAdornment>
            )
          }}
          sx={{ mb: 2 }}
        />

        <TableContainer component={Paper} sx={{ maxHeight: 'calc(100vh - 300px)' }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Updated</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Documents</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading && !refreshing ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : jobs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                    <Typography variant="body1">
                      No jobs found. 
                      {searchQuery ? ' Try a different search query.' : ' Create a job to get started.'}
                    </Typography>
                    {!searchQuery && (
                      <Button
                        variant="contained"
                        startIcon={<CloudUpload />}
                        onClick={handleCreateJob}
                        sx={{ mt: 2 }}
                      >
                        Upload Documents
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ) : (
                jobs.map((job) => (
                  <TableRow 
                    key={job.id} 
                    hover
                    onClick={() => handleViewJob(job.id)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell>{job.id}</TableCell>
                    <TableCell>
                      <Chip 
                        label={job.status} 
                        color={statusColors[job.status] || 'default'} 
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{formatDate(job.createdAt)}</TableCell>
                    <TableCell>{formatDate(job.updatedAt)}</TableCell>
                    <TableCell>{job.jobType || 'Document Processing'}</TableCell>
                    <TableCell>{job.documentCount || 0}</TableCell>
                    <TableCell align="right">
                      <Tooltip title="View details">
                        <IconButton 
                          size="small" 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewJob(job.id);
                          }}
                        >
                          <Visibility />
                        </IconButton>
                      </Tooltip>
                      {job.status !== 'PROCESSING' && (
                        <Tooltip title="Delete job">
                          <IconButton 
                            size="small" 
                            color="error"
                            onClick={(e) => openDeleteDialog(job.id, e)}
                          >
                            <Delete />
                          </IconButton>
                        </Tooltip>
                      )}
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
          count={totalCount}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={closeDeleteDialog}
      >
        <DialogTitle>Delete Job</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this job? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDeleteDialog}>Cancel</Button>
          <Button onClick={handleDeleteJob} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Jobs; 