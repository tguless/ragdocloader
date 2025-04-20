import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Typography,
  Paper,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  Divider,
  Card,
  CardContent,
  Stack,
  Tooltip,
  FormHelperText,
  LinearProgress
} from '@mui/material';
import {
  CloudUpload,
  ArrowBack,
  ArrowForward,
  Description,
  Delete,
  CheckCircle,
  Send,
  Check
} from '@mui/icons-material';
import axios from 'axios';

// Step components
const JobTypeStep = ({ jobType, setJobType, jobTypes, error }) => {
  return (
    <Box sx={{ my: 2 }}>
      <Typography variant="h6" gutterBottom>
        Select Job Type
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Choose the type of document processing job you want to create.
      </Typography>
      
      <FormControl fullWidth error={!!error} sx={{ mt: 2 }}>
        <InputLabel id="job-type-label">Job Type</InputLabel>
        <Select
          labelId="job-type-label"
          id="job-type"
          value={jobType}
          label="Job Type"
          onChange={(e) => setJobType(e.target.value)}
        >
          {jobTypes.map((type) => (
            <MenuItem key={type.id} value={type.id}>
              {type.name}
            </MenuItem>
          ))}
        </Select>
        {error && <FormHelperText>{error}</FormHelperText>}
      </FormControl>
      
      {jobType && (
        <Card variant="outlined" sx={{ mt: 3 }}>
          <CardContent>
            <Typography variant="subtitle1" gutterBottom>
              {jobTypes.find(t => t.id === jobType)?.name}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {jobTypes.find(t => t.id === jobType)?.description}
            </Typography>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

const JobDetailsStep = ({ jobName, setJobName, jobDescription, setJobDescription, errors }) => {
  return (
    <Box sx={{ my: 2 }}>
      <Typography variant="h6" gutterBottom>
        Job Details
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Provide a name and description for your job.
      </Typography>
      
      <Stack spacing={3} sx={{ mt: 2 }}>
        <TextField
          fullWidth
          label="Job Name"
          value={jobName}
          onChange={(e) => setJobName(e.target.value)}
          error={!!errors.jobName}
          helperText={errors.jobName}
          required
        />
        
        <TextField
          fullWidth
          label="Job Description"
          value={jobDescription}
          onChange={(e) => setJobDescription(e.target.value)}
          error={!!errors.jobDescription}
          helperText={errors.jobDescription}
          multiline
          rows={3}
        />
      </Stack>
    </Box>
  );
};

const FileUploadStep = ({ files, setFiles, uploadProgress, uploadError }) => {
  const handleFileChange = (event) => {
    if (event.target.files) {
      const newFiles = Array.from(event.target.files);
      setFiles([...files, ...newFiles]);
    }
  };
  
  const handleRemoveFile = (index) => {
    const newFiles = [...files];
    newFiles.splice(index, 1);
    setFiles(newFiles);
  };
  
  return (
    <Box sx={{ my: 2 }}>
      <Typography variant="h6" gutterBottom>
        Upload Documents
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Upload the documents you want to process. Accepted formats: PDF, DOCX, TXT, etc.
      </Typography>
      
      <Box
        sx={{
          border: '2px dashed #ccc',
          borderRadius: 2,
          p: 3,
          textAlign: 'center',
          backgroundColor: '#fafafa',
          cursor: 'pointer',
          '&:hover': {
            backgroundColor: '#f0f0f0',
          },
          mt: 2,
        }}
        component="label"
      >
        <input
          type="file"
          onChange={handleFileChange}
          style={{ display: 'none' }}
          multiple
          accept=".pdf,.docx,.doc,.txt,.csv,.xlsx,.xls"
        />
        <CloudUpload fontSize="large" color="primary" />
        <Typography variant="body1" sx={{ mt: 1 }}>
          Click to select files or drag and drop
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Accepted formats: PDF, DOCX, TXT, CSV, XLSX
        </Typography>
      </Box>
      
      {uploadError && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {uploadError}
        </Alert>
      )}
      
      {files.length > 0 && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="subtitle2" gutterBottom>
            Selected Files ({files.length})
          </Typography>
          
          <Paper variant="outlined" sx={{ maxHeight: 300, overflow: 'auto' }}>
            {files.map((file, index) => (
              <Box
                key={index}
                sx={{
                  p: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  borderBottom: index < files.length - 1 ? '1px solid #eee' : 'none',
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', overflow: 'hidden' }}>
                  <Typography variant="body2" noWrap sx={{ maxWidth: '70%' }}>
                    {file.name}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                    ({(file.size / 1024 / 1024).toFixed(2)} MB)
                  </Typography>
                </Box>
                
                <IconButton size="small" onClick={() => handleRemoveFile(index)}>
                  <Delete fontSize="small" />
                </IconButton>
              </Box>
            ))}
          </Paper>
        </Box>
      )}
      
      {uploadProgress > 0 && (
        <Box sx={{ mt: 2 }}>
          <Typography variant="body2" align="center">
            Uploading {uploadProgress}%
          </Typography>
          <LinearProgress variant="determinate" value={uploadProgress} sx={{ mt: 1 }} />
        </Box>
      )}
    </Box>
  );
};

const JobConfigStep = ({ jobType, config, setConfig, jobTypes, errors }) => {
  const jobTypeConfig = jobTypes.find(t => t.id === jobType)?.configOptions || [];
  
  const handleConfigChange = (name, value) => {
    setConfig(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  return (
    <Box sx={{ my: 2 }}>
      <Typography variant="h6" gutterBottom>
        Job Configuration
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Configure processing options for your job.
      </Typography>
      
      {jobTypeConfig.length === 0 ? (
        <Alert severity="info" sx={{ mt: 2 }}>
          No configuration options available for this job type
        </Alert>
      ) : (
        <Stack spacing={3} sx={{ mt: 2 }}>
          {jobTypeConfig.map((option) => {
            switch (option.type) {
              case 'select':
                return (
                  <FormControl 
                    key={option.name} 
                    fullWidth 
                    error={!!errors[option.name]}
                  >
                    <InputLabel id={`${option.name}-label`}>{option.label}</InputLabel>
                    <Select
                      labelId={`${option.name}-label`}
                      id={option.name}
                      value={config[option.name] || ''}
                      label={option.label}
                      onChange={(e) => handleConfigChange(option.name, e.target.value)}
                    >
                      {option.options.map((opt) => (
                        <MenuItem key={opt.value} value={opt.value}>
                          {opt.label}
                        </MenuItem>
                      ))}
                    </Select>
                    {option.description && (
                      <FormHelperText>{option.description}</FormHelperText>
                    )}
                    {errors[option.name] && (
                      <FormHelperText error>{errors[option.name]}</FormHelperText>
                    )}
                  </FormControl>
                );
              case 'boolean':
                return (
                  <FormControl 
                    key={option.name} 
                    fullWidth 
                    error={!!errors[option.name]}
                  >
                    <InputLabel id={`${option.name}-label`}>{option.label}</InputLabel>
                    <Select
                      labelId={`${option.name}-label`}
                      id={option.name}
                      value={config[option.name] === undefined ? '' : config[option.name] ? 'true' : 'false'}
                      label={option.label}
                      onChange={(e) => handleConfigChange(option.name, e.target.value === 'true')}
                    >
                      <MenuItem value="true">Yes</MenuItem>
                      <MenuItem value="false">No</MenuItem>
                    </Select>
                    {option.description && (
                      <FormHelperText>{option.description}</FormHelperText>
                    )}
                    {errors[option.name] && (
                      <FormHelperText error>{errors[option.name]}</FormHelperText>
                    )}
                  </FormControl>
                );
              default:
                return (
                  <TextField
                    key={option.name}
                    fullWidth
                    label={option.label}
                    value={config[option.name] || ''}
                    onChange={(e) => handleConfigChange(option.name, e.target.value)}
                    error={!!errors[option.name]}
                    helperText={errors[option.name] || option.description}
                    type={option.type === 'number' ? 'number' : 'text'}
                    inputProps={option.type === 'number' ? {
                      min: option.min,
                      max: option.max,
                      step: option.step || 1
                    } : {}}
                  />
                );
            }
          })}
        </Stack>
      )}
    </Box>
  );
};

const ReviewStep = ({ jobType, jobName, jobDescription, files, config, jobTypes }) => {
  const selectedJobType = jobTypes.find(t => t.id === jobType);
  
  return (
    <Box sx={{ my: 2 }}>
      <Typography variant="h6" gutterBottom>
        Review and Submit
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Review your job details before submission.
      </Typography>
      
      <Stack spacing={3} sx={{ mt: 3 }}>
        <Box>
          <Typography variant="subtitle2" gutterBottom>Job Type</Typography>
          <Typography variant="body1">{selectedJobType?.name}</Typography>
        </Box>
        
        <Divider />
        
        <Box>
          <Typography variant="subtitle2" gutterBottom>Job Details</Typography>
          <Typography variant="body1">{jobName}</Typography>
          {jobDescription && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {jobDescription}
            </Typography>
          )}
        </Box>
        
        <Divider />
        
        <Box>
          <Typography variant="subtitle2" gutterBottom>Documents ({files.length})</Typography>
          {files.length > 0 ? (
            <Box component="ul" sx={{ m: 0, pl: 2 }}>
              {files.map((file, index) => (
                <Typography key={index} variant="body2" component="li">
                  {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                </Typography>
              ))}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No documents selected
            </Typography>
          )}
        </Box>
        
        <Divider />
        
        <Box>
          <Typography variant="subtitle2" gutterBottom>Configuration</Typography>
          {Object.keys(config).length > 0 ? (
            <Box>
              {Object.entries(config).map(([key, value]) => {
                const configOption = selectedJobType?.configOptions.find(opt => opt.name === key);
                if (!configOption) return null;
                
                let displayValue = value;
                
                if (configOption.type === 'select') {
                  const option = configOption.options.find(opt => opt.value === value);
                  displayValue = option ? option.label : value;
                } else if (configOption.type === 'boolean') {
                  displayValue = value ? 'Yes' : 'No';
                }
                
                return (
                  <Box key={key} sx={{ display: 'flex', mt: 1 }}>
                    <Typography variant="body2" sx={{ minWidth: 200, fontWeight: 500 }}>
                      {configOption.label}:
                    </Typography>
                    <Typography variant="body2">
                      {displayValue}
                    </Typography>
                  </Box>
                );
              })}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary">
              Default configuration
            </Typography>
          )}
        </Box>
      </Stack>
    </Box>
  );
};

// Main component
const CreateJob = () => {
  const navigate = useNavigate();
  
  // Step State
  const [activeStep, setActiveStep] = useState(0);
  const steps = ['Type', 'Details', 'Documents', 'Configuration', 'Review'];
  
  // Form State
  const [jobType, setJobType] = useState('');
  const [jobName, setJobName] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [files, setFiles] = useState([]);
  const [config, setConfig] = useState({});
  
  // UI State
  const [loading, setLoading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [formErrors, setFormErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  
  // Mock job types (in a real app, this would come from an API)
  const [jobTypes, setJobTypes] = useState([
    {
      id: 'document_ocr',
      name: 'Document OCR',
      description: 'Extract text from documents using Optical Character Recognition (OCR).',
      configOptions: [
        {
          name: 'language',
          label: 'OCR Language',
          type: 'select',
          options: [
            { value: 'eng', label: 'English' },
            { value: 'fra', label: 'French' },
            { value: 'spa', label: 'Spanish' },
            { value: 'deu', label: 'German' },
          ],
          description: 'Primary language of the documents'
        },
        {
          name: 'quality',
          label: 'OCR Quality',
          type: 'select',
          options: [
            { value: 'high', label: 'High (Slower)' },
            { value: 'medium', label: 'Medium' },
            { value: 'low', label: 'Low (Faster)' },
          ],
          description: 'Balance between accuracy and processing speed'
        },
        {
          name: 'extractLayout',
          label: 'Extract Layout',
          type: 'boolean',
          description: 'Attempt to preserve document layout in extracted text'
        }
      ]
    },
    {
      id: 'data_extraction',
      name: 'Data Extraction',
      description: 'Extract structured data from documents such as forms, invoices, and receipts.',
      configOptions: [
        {
          name: 'template',
          label: 'Extraction Template',
          type: 'select',
          options: [
            { value: 'invoice', label: 'Invoice' },
            { value: 'receipt', label: 'Receipt' },
            { value: 'form', label: 'Generic Form' },
            { value: 'custom', label: 'Custom' },
          ],
          description: 'Document template to use for extraction'
        },
        {
          name: 'confidence',
          label: 'Minimum Confidence',
          type: 'number',
          min: 0,
          max: 100,
          step: 5,
          description: 'Minimum confidence threshold (%) for data extraction'
        }
      ]
    },
    {
      id: 'document_classification',
      name: 'Document Classification',
      description: 'Categorize documents into predefined classes.',
      configOptions: [
        {
          name: 'categories',
          label: 'Categories',
          type: 'select',
          options: [
            { value: 'finance', label: 'Financial Documents' },
            { value: 'legal', label: 'Legal Documents' },
            { value: 'medical', label: 'Medical Documents' },
            { value: 'general', label: 'General Documents' },
          ],
          description: 'Document categories to classify into'
        },
        {
          name: 'confidenceThreshold',
          label: 'Confidence Threshold',
          type: 'number',
          min: 0,
          max: 100,
          description: 'Minimum confidence percentage to assign a category'
        }
      ]
    }
  ]);
  
  // Validate current step
  const validateStep = () => {
    const errors = {};
    
    switch (activeStep) {
      case 0: // Type
        if (!jobType) errors.jobType = 'Please select a job type';
        break;
      case 1: // Details
        if (!jobName.trim()) errors.jobName = 'Job name is required';
        break;
      case 2: // Documents
        if (files.length === 0) errors.files = 'Please upload at least one document';
        break;
      case 3: // Configuration
        // Validate required config fields based on job type
        const requiredFields = jobTypes.find(t => t.id === jobType)?.configOptions.filter(opt => opt.required) || [];
        requiredFields.forEach(field => {
          if (!config[field.name]) {
            errors[field.name] = `${field.label} is required`;
          }
        });
        break;
      default:
        break;
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };
  
  // Handle step navigation
  const handleNext = () => {
    if (validateStep()) {
      setActiveStep((prevActiveStep) => prevActiveStep + 1);
    }
  };
  
  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };
  
  // Handle form submission
  const handleSubmit = async () => {
    setLoading(true);
    setGeneralError('');
    
    try {
      // First, upload files to get document IDs
      const formData = new FormData();
      files.forEach((file) => {
        formData.append('files', file);
      });
      
      // Upload documents
      const uploadResponse = await axios.post('/api/documents/upload', formData, {
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percentCompleted);
        }
      });
      
      const documentIds = uploadResponse.data.documentIds;
      
      // Create job with document IDs
      const jobResponse = await axios.post('/api/jobs', {
        name: jobName,
        description: jobDescription,
        type: jobType,
        config: config,
        documentIds: documentIds
      });
      
      // Navigate to the job details page
      navigate(`/jobs/${jobResponse.data.jobId}`);
    } catch (err) {
      console.error('Error creating job:', err);
      setGeneralError(err.response?.data?.message || 'Failed to create job. Please try again.');
    } finally {
      setLoading(false);
      setUploadProgress(0);
    }
  };
  
  // Render step content
  const getStepContent = (step) => {
    switch (step) {
      case 0:
        return (
          <JobTypeStep 
            jobType={jobType} 
            setJobType={setJobType} 
            jobTypes={jobTypes} 
            error={formErrors.jobType}
          />
        );
      case 1:
        return (
          <JobDetailsStep 
            jobName={jobName} 
            setJobName={setJobName} 
            jobDescription={jobDescription} 
            setJobDescription={setJobDescription} 
            errors={formErrors}
          />
        );
      case 2:
        return (
          <FileUploadStep 
            files={files} 
            setFiles={setFiles} 
            uploadProgress={uploadProgress} 
            uploadError={formErrors.files}
          />
        );
      case 3:
        return (
          <JobConfigStep 
            jobType={jobType} 
            config={config} 
            setConfig={setConfig} 
            jobTypes={jobTypes} 
            errors={formErrors}
          />
        );
      case 4:
        return (
          <ReviewStep 
            jobType={jobType} 
            jobName={jobName} 
            jobDescription={jobDescription} 
            files={files} 
            config={config} 
            jobTypes={jobTypes}
          />
        );
      default:
        return 'Unknown step';
    }
  };
  
  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton 
          sx={{ mr: 1 }} 
          onClick={() => navigate('/jobs')}
          aria-label="Back to jobs"
        >
          <ArrowBack />
        </IconButton>
        <Typography variant="h5" component="h1">
          Create New Job
        </Typography>
      </Box>
      
      {generalError && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {generalError}
        </Alert>
      )}
      
      <Paper sx={{ p: 3 }}>
        <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        <Divider sx={{ mb: 3 }} />
        
        {getStepContent(activeStep)}
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4, pt: 2, borderTop: '1px solid #eee' }}>
          <Button
            disabled={activeStep === 0 || loading}
            onClick={handleBack}
            startIcon={<ArrowBack />}
          >
            Back
          </Button>
          
          <Box>
            <Button 
              onClick={() => navigate('/jobs')} 
              sx={{ mr: 1 }}
              disabled={loading}
            >
              Cancel
            </Button>
            
            {activeStep === steps.length - 1 ? (
              <Button
                variant="contained"
                color="primary"
                onClick={handleSubmit}
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <Check />}
              >
                Submit Job
              </Button>
            ) : (
              <Button
                variant="contained"
                color="primary"
                onClick={handleNext}
                endIcon={<ArrowForward />}
                disabled={loading}
              >
                Next
              </Button>
            )}
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default CreateJob; 