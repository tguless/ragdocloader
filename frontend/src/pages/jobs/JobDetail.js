import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/axiosConfig';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  Grid,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
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
  LinearProgress,
  Card,
  CardContent,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  ArrowBack,
  Refresh,
  Description,
  Delete,
  Cancel,
  PlayArrow,
  Download,
  History,
  ViewList,
  Settings,
  Timeline,
  ExpandMore,
  Info,
  CheckCircle,
  Error as ErrorIcon,
  Pause,
} from '@mui/icons-material';

// Helper function to format dates
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

// Custom TabPanel component
const TabPanel = (props) => {
  const { children, value, index, ...other } = props;
  
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`job-tabpanel-${index}`}
      aria-labelledby={`job-tab-${index}`}
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

// Status chip with appropriate color
const StatusChip = ({ status }) => {
  let color = 'default';
  let icon = null;
  
  switch (status.toLowerCase()) {
    case 'completed':
      color = 'success';
      icon = <CheckCircle fontSize="small" />;
      break;
    case 'processing':
    case 'running':
      color = 'primary';
      break;
    case 'failed':
      color = 'error';
      icon = <ErrorIcon fontSize="small" />;
      break;
    case 'pending':
      color = 'warning';
      break;
    case 'cancelled':
      color = 'default';
      break;
    case 'paused':
      color = 'info';
      icon = <Pause fontSize="small" />;
      break;
    default:
      color = 'default';
  }
  
  return (
    <Chip 
      label={status} 
      color={color} 
      size="small" 
      icon={icon}
      sx={{ textTransform: 'capitalize' }}
    />
  );
};

// Documents list component
const DocumentsList = ({ documents, onDownload }) => {
  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Document Name</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Pages</TableCell>
            <TableCell>Size</TableCell>
            <TableCell>Status</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {documents.map((doc) => (
            <TableRow key={doc.id}>
              <TableCell>{doc.name}</TableCell>
              <TableCell>{doc.type}</TableCell>
              <TableCell>{doc.pages || '-'}</TableCell>
              <TableCell>{(doc.size / 1024 / 1024).toFixed(2)} MB</TableCell>
              <TableCell>
                <StatusChip status={doc.status} />
              </TableCell>
              <TableCell align="right">
                <IconButton 
                  size="small" 
                  onClick={() => onDownload(doc.id, doc.name)}
                  disabled={doc.status !== 'completed'}
                >
                  <Download fontSize="small" />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

// Job log component
const JobLogs = ({ logs }) => {
  return (
    <Paper 
      variant="outlined" 
      sx={{ 
        height: 300, 
        overflow: 'auto',
        p: 2,
        backgroundColor: '#f5f5f5', 
        fontFamily: 'monospace',
        fontSize: '0.875rem'
      }}
    >
      {logs.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
          No logs available
        </Typography>
      ) : (
        logs.map((log, index) => (
          <Box key={index} sx={{ mb: 1 }}>
            <Typography variant="caption" component="span" sx={{ color: 'text.secondary', mr: 1 }}>
              [{formatDate(log.timestamp)}]
            </Typography>
            <Typography 
              variant="body2" 
              component="span" 
              sx={{ 
                color: log.level === 'ERROR' ? 'error.main' : 
                       log.level === 'WARNING' ? 'warning.main' : 'text.primary' 
              }}
            >
              {log.message}
            </Typography>
          </Box>
        ))
      )}
    </Paper>
  );
};

// Main component
const JobDetail = () => {
  const { jobId } = useParams();
  const navigate = useNavigate();
  
  // Tab state
  const [tabValue, setTabValue] = useState(0);
  
  // Job state
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);
  
  // Document state
  const [documents, setDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  
  // Results state
  const [results, setResults] = useState([]);
  const [resultsLoading, setResultsLoading] = useState(false);
  
  // Logs state
  const [logs, setLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);
  
  // Dialog state
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [actionInProgress, setActionInProgress] = useState(false);
  
  // Load job details
  useEffect(() => {
    fetchJobDetails();
  }, [jobId]);
  
  // Fetch data based on active tab
  useEffect(() => {
    if (!job) return;
    
    switch (tabValue) {
      case 1: // Documents
        fetchDocuments();
        break;
      case 2: // Results
        fetchResults();
        break;
      case 3: // Logs
        fetchLogs();
        break;
      default:
        break;
    }
  }, [tabValue, job, fetchDocuments, fetchResults, fetchLogs]);
  
  // Fetch job details
  const fetchJobDetails = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await api.get(`/jobs/${jobId}`);
      setJob(response.data);
    } catch (err) {
      console.error('Error fetching job details:', err);
      setError(err.response?.data?.message || 'Failed to load job details. Please try again.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };
  
  // Fetch job documents
  const fetchDocuments = async () => {
    if (documents.length > 0 && !refreshing) return;
    
    setDocumentsLoading(true);
    
    try {
      const response = await api.get(`/jobs/${jobId}/documents`);
      setDocuments(response.data);
    } catch (err) {
      console.error('Error fetching documents:', err);
    } finally {
      setDocumentsLoading(false);
    }
  };
  
  // Fetch job results
  const fetchResults = async () => {
    if (results.length > 0 && !refreshing) return;
    
    setResultsLoading(true);
    
    try {
      const response = await api.get(`/jobs/${jobId}/results`);
      setResults(response.data);
    } catch (err) {
      console.error('Error fetching results:', err);
    } finally {
      setResultsLoading(false);
    }
  };
  
  // Fetch job logs
  const fetchLogs = async () => {
    if (logs.length > 0 && !refreshing) return;
    
    setLogsLoading(true);
    
    try {
      const response = await api.get(`/jobs/${jobId}/logs`);
      setLogs(response.data);
    } catch (err) {
      console.error('Error fetching logs:', err);
    } finally {
      setLogsLoading(false);
    }
  };
  
  // Refresh all data
  const handleRefresh = () => {
    setRefreshing(true);
    fetchJobDetails();
    
    if (tabValue === 1) {
      setDocuments([]);
      fetchDocuments();
    } else if (tabValue === 2) {
      setResults([]);
      fetchResults();
    } else if (tabValue === 3) {
      setLogs([]);
      fetchLogs();
    }
  };
  
  // Handle tab change
  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };
  
  // Download document
  const handleDownloadDocument = async (documentId, filename) => {
    try {
      const response = await api.get(`/documents/${documentId}/download`, {
        responseType: 'blob'
      });
      
      // Create a download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error('Error downloading document:', err);
    }
  };
  
  // Download result
  const handleDownloadResult = async (resultId, filename) => {
    try {
      const response = await api.get(`/results/${resultId}/download`, {
        responseType: 'blob'
      });
      
      // Create a download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error('Error downloading result:', err);
    }
  };
  
  // Cancel job
  const handleCancelJob = async () => {
    setActionInProgress(true);
    
    try {
      await api.post(`/jobs/${jobId}/cancel`);
      setShowCancelDialog(false);
      handleRefresh();
    } catch (err) {
      console.error('Error cancelling job:', err);
    } finally {
      setActionInProgress(false);
    }
  };
  
  // Resume job
  const handleResumeJob = async () => {
    setActionInProgress(true);
    
    try {
      await api.post(`/jobs/${jobId}/resume`);
      handleRefresh();
    } catch (err) {
      console.error('Error resuming job:', err);
    } finally {
      setActionInProgress(false);
    }
  };
  
  // Delete job
  const handleDeleteJob = async () => {
    setActionInProgress(true);
    
    try {
      await api.delete(`/jobs/${jobId}`);
      setShowDeleteDialog(false);
      navigate('/jobs');
    } catch (err) {
      console.error('Error deleting job:', err);
    } finally {
      setActionInProgress(false);
    }
  };
  
  // Calculate job progress percentage
  const calculateProgressPercentage = (job) => {
    if (!job) return 0;
    
    switch (job.status.toLowerCase()) {
      case 'completed':
        return 100;
      case 'failed':
      case 'cancelled':
        return job.progress || 0;
      case 'processing':
      case 'running':
        return job.progress || 50; // Default to 50% if no progress info
      default:
        return 0;
    }
  };
  
  // Render the component
  if (loading) {
    return (
      <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <CircularProgress sx={{ mb: 2 }} />
        <Typography variant="body1">Loading job details...</Typography>
      </Box>
    );
  }
  
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/jobs')}>
          Back to Jobs
        </Button>
      </Box>
    );
  }
  
  if (!job) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Job not found.
        </Alert>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/jobs')}>
          Back to Jobs
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
            onClick={() => navigate('/jobs')} 
            sx={{ mr: 1 }}
            aria-label="Back to jobs"
          >
            <ArrowBack />
          </IconButton>
          <Typography variant="h5" component="h1">
            Job Details
          </Typography>
        </Box>
        <Button 
          startIcon={<Refresh />} 
          onClick={handleRefresh}
          disabled={refreshing}
        >
          Refresh
        </Button>
      </Box>
      
      {/* Job summary card */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={8}>
            <Typography variant="h5" gutterBottom>
              {job.name}
            </Typography>
            
            {job.description && (
              <Typography variant="body2" color="text.secondary" paragraph>
                {job.description}
              </Typography>
            )}
            
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Job ID
                </Typography>
                <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
                  {job.id}
                </Typography>
              </Grid>
              
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Job Type
                </Typography>
                <Typography variant="body2">
                  {job.type}
                </Typography>
              </Grid>
              
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Status
                </Typography>
                <StatusChip status={job.status} />
              </Grid>
              
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Created
                </Typography>
                <Typography variant="body2">
                  {formatDate(job.createdAt)}
                </Typography>
              </Grid>
              
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Started
                </Typography>
                <Typography variant="body2">
                  {job.startedAt ? formatDate(job.startedAt) : 'Not started'}
                </Typography>
              </Grid>
              
              <Grid item xs={6} sm={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Completed
                </Typography>
                <Typography variant="body2">
                  {job.completedAt ? formatDate(job.completedAt) : 'Not completed'}
                </Typography>
              </Grid>
            </Grid>
            
            {/* Progress bar */}
            {['processing', 'running'].includes(job.status.toLowerCase()) && (
              <Box sx={{ mt: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Progress</Typography>
                  <Typography variant="body2">{calculateProgressPercentage(job)}%</Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={calculateProgressPercentage(job)} 
                  sx={{ height: 8, borderRadius: 4 }}
                />
              </Box>
            )}
          </Grid>
          
          <Grid item xs={12} md={4}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                  Actions
                </Typography>
                
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {job.status.toLowerCase() === 'processing' || job.status.toLowerCase() === 'running' ? (
                    <Button 
                      startIcon={<Cancel />} 
                      variant="outlined" 
                      color="warning"
                      onClick={() => setShowCancelDialog(true)}
                      disabled={actionInProgress}
                      fullWidth
                    >
                      Cancel Job
                    </Button>
                  ) : null}
                  
                  {job.status.toLowerCase() === 'paused' || job.status.toLowerCase() === 'pending' ? (
                    <Button 
                      startIcon={<PlayArrow />} 
                      variant="outlined" 
                      color="primary"
                      onClick={handleResumeJob}
                      disabled={actionInProgress}
                      fullWidth
                    >
                      Resume Job
                    </Button>
                  ) : null}
                  
                  {job.status.toLowerCase() === 'completed' ? (
                    <Button 
                      startIcon={<Download />} 
                      variant="outlined" 
                      color="primary"
                      onClick={() => setTabValue(2)} // Switch to results tab
                      fullWidth
                    >
                      View Results
                    </Button>
                  ) : null}
                  
                  <Button 
                    startIcon={<Delete />} 
                    variant="outlined" 
                    color="error"
                    onClick={() => setShowDeleteDialog(true)}
                    disabled={actionInProgress}
                    fullWidth
                  >
                    Delete Job
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Job details tabs */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab icon={<Info />} label="Overview" iconPosition="start" />
          <Tab icon={<Description />} label="Documents" iconPosition="start" />
          <Tab icon={<ViewList />} label="Results" iconPosition="start" />
          <Tab icon={<History />} label="Logs" iconPosition="start" />
          <Tab icon={<Settings />} label="Configuration" iconPosition="start" />
          <Tab icon={<Timeline />} label="Timeline" iconPosition="start" />
        </Tabs>
        
        {/* Overview Tab */}
        <TabPanel value={tabValue} index={0}>
          <Typography variant="h6" gutterBottom>
            Job Overview
          </Typography>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card variant="outlined" sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    Documents Summary
                  </Typography>
                  
                  {documentsLoading ? (
                    <CircularProgress size={24} sx={{ my: 2 }} />
                  ) : (
                    <List dense>
                      <ListItem>
                        <ListItemText 
                          primary="Total Documents" 
                          secondary={job.documentCount || '0'} 
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText 
                          primary="Processed Documents" 
                          secondary={job.processedDocumentCount || '0'} 
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText 
                          primary="Failed Documents" 
                          secondary={job.failedDocumentCount || '0'} 
                        />
                      </ListItem>
                    </List>
                  )}
                  
                  <Button 
                    size="small" 
                    onClick={() => setTabValue(1)}
                    sx={{ mt: 1 }}
                  >
                    View Documents
                  </Button>
                </CardContent>
              </Card>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Card variant="outlined" sx={{ height: '100%' }}>
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    Results Summary
                  </Typography>
                  
                  {resultsLoading ? (
                    <CircularProgress size={24} sx={{ my: 2 }} />
                  ) : (
                    <List dense>
                      <ListItem>
                        <ListItemText 
                          primary="Total Results" 
                          secondary={results.length || '0'} 
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText 
                          primary="Result Size" 
                          secondary={job.resultSize ? `${(job.resultSize / 1024 / 1024).toFixed(2)} MB` : 'N/A'} 
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText 
                          primary="Result Type" 
                          secondary={job.resultType || 'N/A'} 
                        />
                      </ListItem>
                    </List>
                  )}
                  
                  <Button 
                    size="small" 
                    onClick={() => setTabValue(2)}
                    sx={{ mt: 1 }}
                    disabled={job.status.toLowerCase() !== 'completed'}
                  >
                    View Results
                  </Button>
                </CardContent>
              </Card>
            </Grid>
            
            {job.stats && (
              <Grid item xs={12}>
                <Card variant="outlined">
                  <CardContent>
                    <Typography variant="subtitle1" gutterBottom>
                      Processing Statistics
                    </Typography>
                    
                    <Grid container spacing={2}>
                      {Object.entries(job.stats).map(([key, value]) => (
                        <Grid item xs={6} sm={4} md={3} key={key}>
                          <Typography variant="subtitle2" color="text.secondary">
                            {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
                          </Typography>
                          <Typography variant="body2">
                            {typeof value === 'number' ? value.toLocaleString() : value.toString()}
                          </Typography>
                        </Grid>
                      ))}
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        </TabPanel>
        
        {/* Documents Tab */}
        <TabPanel value={tabValue} index={1}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">
              Documents ({documents.length})
            </Typography>
          </Box>
          
          {documentsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : documents.length > 0 ? (
            <DocumentsList 
              documents={documents} 
              onDownload={handleDownloadDocument} 
            />
          ) : (
            <Alert severity="info">No documents found for this job.</Alert>
          )}
        </TabPanel>
        
        {/* Results Tab */}
        <TabPanel value={tabValue} index={2}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">
              Results ({results.length})
            </Typography>
          </Box>
          
          {job.status.toLowerCase() !== 'completed' ? (
            <Alert severity="info" sx={{ mb: 2 }}>
              Results will be available once the job is completed.
            </Alert>
          ) : resultsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : results.length > 0 ? (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Result Name</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Size</TableCell>
                    <TableCell>Created At</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.map((result) => (
                    <TableRow key={result.id}>
                      <TableCell>{result.name}</TableCell>
                      <TableCell>{result.type}</TableCell>
                      <TableCell>{(result.size / 1024 / 1024).toFixed(2)} MB</TableCell>
                      <TableCell>{formatDate(result.createdAt)}</TableCell>
                      <TableCell align="right">
                        <IconButton 
                          size="small" 
                          onClick={() => handleDownloadResult(result.id, result.name)}
                        >
                          <Download fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Alert severity="info">No results available for this job.</Alert>
          )}
        </TabPanel>
        
        {/* Logs Tab */}
        <TabPanel value={tabValue} index={3}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
            <Typography variant="h6">
              Processing Logs
            </Typography>
          </Box>
          
          {logsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <JobLogs logs={logs} />
          )}
        </TabPanel>
        
        {/* Configuration Tab */}
        <TabPanel value={tabValue} index={4}>
          <Typography variant="h6" gutterBottom>
            Job Configuration
          </Typography>
          
          {job.config ? (
            <Card variant="outlined">
              <CardContent>
                <Grid container spacing={2}>
                  {Object.entries(job.config).map(([key, value]) => (
                    <Grid item xs={12} sm={6} md={4} key={key}>
                      <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                        {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
                      </Typography>
                      <Typography variant="body2">
                        {typeof value === 'boolean' 
                          ? (value ? 'Yes' : 'No')
                          : (value === null ? 'None' : value.toString())
                        }
                      </Typography>
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          ) : (
            <Alert severity="info">No configuration settings available for this job.</Alert>
          )}
        </TabPanel>
        
        {/* Timeline Tab */}
        <TabPanel value={tabValue} index={5}>
          <Typography variant="h6" gutterBottom>
            Job Timeline
          </Typography>
          
          {job.timeline ? (
            <Box>
              {job.timeline.map((event, index) => (
                <Accordion key={index} disableGutters variant="outlined" sx={{ mb: 1 }}>
                  <AccordionSummary expandIcon={<ExpandMore />}>
                    <Grid container>
                      <Grid item xs={4}>
                        <Typography variant="subtitle2">{event.status}</Typography>
                      </Grid>
                      <Grid item xs={8}>
                        <Typography variant="body2" color="text.secondary">
                          {formatDate(event.timestamp)}
                        </Typography>
                      </Grid>
                    </Grid>
                  </AccordionSummary>
                  <AccordionDetails>
                    {event.details && (
                      <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                        {event.details}
                      </Typography>
                    )}
                    {event.metadata && Object.keys(event.metadata).length > 0 && (
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="subtitle2" gutterBottom>Metadata</Typography>
                        <List dense disablePadding>
                          {Object.entries(event.metadata).map(([key, value]) => (
                            <ListItem key={key} disablePadding sx={{ py: 0.5 }}>
                              <ListItemText 
                                primary={`${key}: ${typeof value === 'object' ? JSON.stringify(value) : value}`}
                                primaryTypographyProps={{ variant: 'body2' }}
                              />
                            </ListItem>
                          ))}
                        </List>
                      </Box>
                    )}
                  </AccordionDetails>
                </Accordion>
              ))}
            </Box>
          ) : (
            <Alert severity="info">No timeline data available for this job.</Alert>
          )}
        </TabPanel>
      </Paper>
      
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        aria-labelledby="delete-job-dialog-title"
      >
        <DialogTitle id="delete-job-dialog-title">
          Delete Job
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete this job? This action cannot be undone and all associated data will be permanently removed.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setShowDeleteDialog(false)} 
            disabled={actionInProgress}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleDeleteJob} 
            color="error" 
            disabled={actionInProgress}
            startIcon={actionInProgress ? <CircularProgress size={20} /> : <Delete />}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
      
      {/* Cancel Confirmation Dialog */}
      <Dialog
        open={showCancelDialog}
        onClose={() => setShowCancelDialog(false)}
        aria-labelledby="cancel-job-dialog-title"
      >
        <DialogTitle id="cancel-job-dialog-title">
          Cancel Job
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to cancel this job? Any progress made so far will be saved, but the job will not complete.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button 
            onClick={() => setShowCancelDialog(false)} 
            disabled={actionInProgress}
          >
            No
          </Button>
          <Button 
            onClick={handleCancelJob} 
            color="warning" 
            disabled={actionInProgress}
            startIcon={actionInProgress ? <CircularProgress size={20} /> : <Cancel />}
          >
            Yes, Cancel Job
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default JobDetail; 