import React from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow,
  Chip,
  IconButton,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  Grid
} from '@mui/material';
import { 
  Add as AddIcon, 
  Visibility as ViewIcon,
  Delete as DeleteIcon,
  Edit as EditIcon
} from '@mui/icons-material';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { format } from 'date-fns';
import jobService from '../../services/jobService';

const getStatusColor = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'PROCESSING':
      return 'info';
    case 'COMPLETED':
      return 'success';
    case 'FAILED':
      return 'error';
    case 'SCHEDULED':
      return 'secondary';
    default:
      return 'default';
  }
};

const JobStatusSummary = ({ jobs }) => {
  const statusCounts = jobs.reduce((acc, job) => {
    acc[job.status] = (acc[job.status] || 0) + 1;
    return acc;
  }, {});

  return (
    <Grid container spacing={2} sx={{ mb: 3 }}>
      {Object.entries(statusCounts).map(([status, count]) => (
        <Grid item xs={6} sm={4} md={2} key={status}>
          <Card>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h4" component="div">
                {count}
              </Typography>
              <Chip 
                label={status} 
                color={getStatusColor(status)} 
                size="small" 
                sx={{ mt: 1 }} 
              />
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

const JobList = () => {
  const queryClient = useQueryClient();
  
  // Fetch all jobs
  const { data: jobs, isLoading, error } = useQuery({
    queryKey: ['jobs'],
    queryFn: jobService.getAllJobs
  });

  // Delete job mutation
  const deleteJobMutation = useMutation({
    mutationFn: jobService.deleteJob,
    onSuccess: () => {
      // Invalidate and refetch jobs after successful deletion
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    }
  });

  const handleDeleteJob = (jobId) => {
    if (window.confirm('Are you sure you want to delete this job?')) {
      deleteJobMutation.mutate(jobId);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        {error.message || 'Failed to load jobs. Please try again later.'}
      </Alert>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Document Jobs
        </Typography>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<AddIcon />}
          component={Link}
          to="/jobs/create"
        >
          New Job
        </Button>
      </Box>

      {jobs && jobs.length > 0 ? (
        <>
          <JobStatusSummary jobs={jobs} />

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Source</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Scheduled</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {jobs.map((job) => (
                  <TableRow key={job.id}>
                    <TableCell>{job.name}</TableCell>
                    <TableCell>
                      <Chip 
                        label={job.status} 
                        color={getStatusColor(job.status)} 
                        size="small" 
                      />
                    </TableCell>
                    <TableCell>{job.sourceLocation}</TableCell>
                    <TableCell>
                      {job.createdAt ? format(new Date(job.createdAt), 'MMM d, yyyy HH:mm') : '-'}
                    </TableCell>
                    <TableCell>
                      {job.scheduledTime ? format(new Date(job.scheduledTime), 'MMM d, yyyy HH:mm') : '-'}
                    </TableCell>
                    <TableCell align="right">
                      <IconButton
                        component={Link}
                        to={`/jobs/${job.id}`}
                        color="primary"
                        title="View details"
                      >
                        <ViewIcon />
                      </IconButton>
                      {job.status !== 'PROCESSING' && (
                        <>
                          <IconButton
                            component={Link}
                            to={`/jobs/${job.id}/edit`}
                            color="secondary"
                            title="Edit job"
                          >
                            <EditIcon />
                          </IconButton>
                          <IconButton
                            color="error"
                            onClick={() => handleDeleteJob(job.id)}
                            title="Delete job"
                            disabled={deleteJobMutation.isPending}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      ) : (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" gutterBottom>
            No jobs found
          </Typography>
          <Typography variant="body1" color="textSecondary" paragraph>
            You haven't created any document processing jobs yet.
          </Typography>
          <Button 
            variant="contained" 
            color="primary" 
            startIcon={<AddIcon />}
            component={Link}
            to="/jobs/create"
            sx={{ mt: 2 }}
          >
            Create Your First Job
          </Button>
        </Paper>
      )}
    </Box>
  );
};

export default JobList; 