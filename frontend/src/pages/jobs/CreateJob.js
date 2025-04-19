import React, { useState } from 'react';
import { 
  Box, 
  Typography, 
  Button, 
  TextField, 
  Paper, 
  Grid,
  Alert,
  CircularProgress,
  FormControlLabel,
  Switch,
  Stack,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { useDropzone } from 'react-dropzone';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import jobService from '../../services/jobService';
import { CloudUpload as CloudUploadIcon } from '@mui/icons-material';

const steps = ['Job Details', 'Configure Source', 'Review & Create'];

const CreateJob = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [files, setFiles] = useState([]);
  const [isScheduled, setIsScheduled] = useState(false);

  // Dropzone configuration
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: {
      'application/pdf': ['.pdf'],
      'application/msword': ['.doc'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx']
    },
    onDrop: (acceptedFiles) => {
      setFiles(acceptedFiles.map(file => Object.assign(file, {
        preview: URL.createObjectURL(file)
      })));
    }
  });

  // Create job mutation
  const createJobMutation = useMutation({
    mutationFn: jobService.createJob,
    onSuccess: () => {
      navigate('/jobs');
    }
  });

  // Formik form setup
  const formik = useFormik({
    initialValues: {
      name: '',
      description: '',
      sourceLocation: '',
      scheduledTime: null
    },
    validationSchema: Yup.object({
      name: Yup.string().required('Name is required'),
      description: Yup.string(),
      sourceLocation: Yup.string().required('Source location is required'),
      scheduledTime: Yup.date().nullable()
    }),
    onSubmit: (values) => {
      // If we had files, we would first upload them here
      // For now we just create the job with the source location
      createJobMutation.mutate(values);
    }
  });

  const handleNext = () => {
    if (activeStep === 0) {
      if (!formik.values.name) {
        formik.setFieldTouched('name', true);
        return;
      }
    } else if (activeStep === 1) {
      if (!formik.values.sourceLocation) {
        formik.setFieldTouched('sourceLocation', true);
        return;
      }
    }
    
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleScheduleToggle = (event) => {
    setIsScheduled(event.target.checked);
    if (!event.target.checked) {
      formik.setFieldValue('scheduledTime', null);
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box>
        <Typography variant="h4" component="h1" gutterBottom>
          Create Document Job
        </Typography>

        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Paper elevation={3} sx={{ p: 4 }}>
          {createJobMutation.isError && (
            <Alert severity="error" sx={{ mb: 3 }}>
              {createJobMutation.error?.message || 'Failed to create job. Please try again.'}
            </Alert>
          )}

          <form onSubmit={formik.handleSubmit}>
            {activeStep === 0 && (
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    id="name"
                    name="name"
                    label="Job Name"
                    variant="outlined"
                    required
                    value={formik.values.name}
                    onChange={formik.handleChange}
                    error={formik.touched.name && Boolean(formik.errors.name)}
                    helperText={formik.touched.name && formik.errors.name}
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    id="description"
                    name="description"
                    label="Description"
                    variant="outlined"
                    multiline
                    rows={4}
                    value={formik.values.description}
                    onChange={formik.handleChange}
                    error={formik.touched.description && Boolean(formik.errors.description)}
                    helperText={formik.touched.description && formik.errors.description}
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={isScheduled}
                        onChange={handleScheduleToggle}
                        name="scheduled"
                        color="primary"
                      />
                    }
                    label="Schedule for later"
                  />
                </Grid>
                {isScheduled && (
                  <Grid item xs={12} sm={6}>
                    <DateTimePicker
                      label="Scheduled Time"
                      value={formik.values.scheduledTime}
                      onChange={(date) => formik.setFieldValue('scheduledTime', date)}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          variant: 'outlined',
                          error: formik.touched.scheduledTime && Boolean(formik.errors.scheduledTime),
                          helperText: formik.touched.scheduledTime && formik.errors.scheduledTime
                        }
                      }}
                    />
                  </Grid>
                )}
              </Grid>
            )}

            {activeStep === 1 && (
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    id="sourceLocation"
                    name="sourceLocation"
                    label="S3 Bucket Location"
                    placeholder="s3://bucket-name/folder-path/"
                    variant="outlined"
                    required
                    value={formik.values.sourceLocation}
                    onChange={formik.handleChange}
                    error={formik.touched.sourceLocation && Boolean(formik.errors.sourceLocation)}
                    helperText={formik.touched.sourceLocation && formik.errors.sourceLocation}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>
                    Or Upload Documents Directly
                  </Typography>
                  <div
                    {...getRootProps()}
                    className={`dropzone ${isDragActive ? 'active-dropzone' : ''}`}
                  >
                    <input {...getInputProps()} />
                    <Box sx={{ textAlign: 'center', py: 3 }}>
                      <CloudUploadIcon sx={{ fontSize: 48, color: 'primary.main', mb: 2 }} />
                      <Typography variant="body1" gutterBottom>
                        Drag & drop files here, or click to select files
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        Supported formats: PDF, DOCX, TXT, XLS, XLSX
                      </Typography>
                    </Box>
                  </div>
                </Grid>
                {files.length > 0 && (
                  <Grid item xs={12}>
                    <Typography variant="h6" gutterBottom>
                      Selected Files ({files.length})
                    </Typography>
                    {files.map((file) => (
                      <Box key={file.name} sx={{ mb: 1 }}>
                        <Typography variant="body2">
                          {file.name} - {(file.size / 1024).toFixed(2)} KB
                        </Typography>
                      </Box>
                    ))}
                  </Grid>
                )}
              </Grid>
            )}

            {activeStep === 2 && (
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>
                    Job Summary
                  </Typography>
                  <Box sx={{ bgcolor: 'background.default', p: 2, borderRadius: 1 }}>
                    <Grid container spacing={2}>
                      <Grid item xs={3}>
                        <Typography variant="body2" color="textSecondary">
                          Name:
                        </Typography>
                      </Grid>
                      <Grid item xs={9}>
                        <Typography variant="body2">
                          {formik.values.name}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={3}>
                        <Typography variant="body2" color="textSecondary">
                          Description:
                        </Typography>
                      </Grid>
                      <Grid item xs={9}>
                        <Typography variant="body2">
                          {formik.values.description || 'N/A'}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={3}>
                        <Typography variant="body2" color="textSecondary">
                          Source:
                        </Typography>
                      </Grid>
                      <Grid item xs={9}>
                        <Typography variant="body2">
                          {formik.values.sourceLocation}
                        </Typography>
                      </Grid>
                      
                      <Grid item xs={3}>
                        <Typography variant="body2" color="textSecondary">
                          Scheduled:
                        </Typography>
                      </Grid>
                      <Grid item xs={9}>
                        <Typography variant="body2">
                          {formik.values.scheduledTime 
                            ? new Date(formik.values.scheduledTime).toLocaleString() 
                            : 'Run immediately'}
                        </Typography>
                      </Grid>
                      
                      {files.length > 0 && (
                        <>
                          <Grid item xs={3}>
                            <Typography variant="body2" color="textSecondary">
                              Files:
                            </Typography>
                          </Grid>
                          <Grid item xs={9}>
                            <Typography variant="body2">
                              {files.length} file(s) selected for upload
                            </Typography>
                          </Grid>
                        </>
                      )}
                    </Grid>
                  </Box>
                </Grid>
              </Grid>
            )}

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
              <Button
                disabled={activeStep === 0}
                onClick={handleBack}
              >
                Back
              </Button>
              <Box>
                {activeStep === steps.length - 1 ? (
                  <Button
                    variant="contained"
                    color="primary"
                    type="submit"
                    disabled={createJobMutation.isPending}
                  >
                    {createJobMutation.isPending ? (
                      <CircularProgress size={24} />
                    ) : (
                      'Create Job'
                    )}
                  </Button>
                ) : (
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleNext}
                  >
                    Next
                  </Button>
                )}
              </Box>
            </Box>
          </form>
        </Paper>
      </Box>
    </LocalizationProvider>
  );
};

export default CreateJob; 