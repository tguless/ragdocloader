import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Formik, Form, Field } from 'formik';
import * as Yup from 'yup';
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Container,
  Alert,
  Link as MuiLink,
  CircularProgress,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import axios from 'axios';

const steps = ['Tenant Details', 'Admin Account'];

const TenantRegistration = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [tenantData, setTenantData] = useState(null);
  const navigate = useNavigate();

  const tenantValidationSchema = Yup.object().shape({
    tenantName: Yup.string().required('Organization name is required'),
    subdomain: Yup.string()
      .required('Subdomain is required')
      .matches(/^[a-z0-9]+(-[a-z0-9]+)*$/, 'Subdomain can only contain lowercase letters, numbers, and hyphens')
      .min(3, 'Subdomain must be at least 3 characters')
  });

  const adminValidationSchema = Yup.object().shape({
    firstName: Yup.string().required('First name is required'),
    lastName: Yup.string().required('Last name is required'),
    email: Yup.string().email('Invalid email format').required('Email is required'),
    password: Yup.string()
      .min(8, 'Password must be at least 8 characters')
      .required('Password is required'),
    confirmPassword: Yup.string()
      .oneOf([Yup.ref('password'), null], 'Passwords must match')
      .required('Confirm password is required')
  });

  const handleTenantSubmit = async (values, { setSubmitting }) => {
    try {
      setError('');
      // Validate tenant details availability
      const response = await axios.post('/api/tenants/validate', {
        name: values.tenantName,
        subdomain: values.subdomain
      });
      
      setTenantData(values);
      setActiveStep(1);
    } catch (error) {
      if (error.response && error.response.data) {
        setError(error.response.data.message || 'Tenant validation failed. Please try another name or subdomain.');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleAdminSubmit = async (values, { setSubmitting }) => {
    try {
      setError('');
      // Register tenant with admin user
      await axios.post('/api/tenants/register', {
        tenant: {
          name: tenantData.tenantName,
          subdomain: tenantData.subdomain
        },
        admin: {
          firstName: values.firstName,
          lastName: values.lastName,
          email: values.email,
          password: values.password
        }
      });
      
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (error) {
      if (error.response && error.response.data) {
        setError(error.response.data.message || 'Registration failed. Please try again.');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleBack = () => {
    setActiveStep(0);
  };

  return (
    <Container component="main" maxWidth="md">
      <Paper elevation={3} sx={{ p: 4, mt: 8 }}>
        <Typography component="h1" variant="h4" align="center" gutterBottom>
          Register New Tenant
        </Typography>
        
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Tenant registration successful! Redirecting to login...
          </Alert>
        )}
        
        {activeStep === 0 && (
          <Formik
            initialValues={{
              tenantName: '',
              subdomain: ''
            }}
            validationSchema={tenantValidationSchema}
            onSubmit={handleTenantSubmit}
          >
            {({ isSubmitting, errors, touched }) => (
              <Form>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                  <Field
                    as={TextField}
                    name="tenantName"
                    label="Organization Name"
                    fullWidth
                    error={errors.tenantName && touched.tenantName}
                    helperText={touched.tenantName && errors.tenantName}
                  />
                  
                  <Field
                    as={TextField}
                    name="subdomain"
                    label="Subdomain"
                    fullWidth
                    error={errors.subdomain && touched.subdomain}
                    helperText={(touched.subdomain && errors.subdomain) || "Your tenant will be accessible at subdomain.docloader.com"}
                    InputProps={{
                      endAdornment: <Typography>.docloader.com</Typography>
                    }}
                  />
                  
                  <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    color="primary"
                    disabled={isSubmitting}
                    sx={{ mt: 2, py: 1.5 }}
                  >
                    {isSubmitting ? <CircularProgress size={24} /> : 'Next'}
                  </Button>
                </Box>
              </Form>
            )}
          </Formik>
        )}
        
        {activeStep === 1 && (
          <Formik
            initialValues={{
              firstName: '',
              lastName: '',
              email: '',
              password: '',
              confirmPassword: ''
            }}
            validationSchema={adminValidationSchema}
            onSubmit={handleAdminSubmit}
          >
            {({ isSubmitting, errors, touched }) => (
              <Form>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Typography variant="subtitle1" sx={{ mb: 1 }}>
                    Admin Account for {tenantData.tenantName} ({tenantData.subdomain}.docloader.com)
                  </Typography>
                  
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <Field
                      as={TextField}
                      name="firstName"
                      label="First Name"
                      fullWidth
                      error={errors.firstName && touched.firstName}
                      helperText={touched.firstName && errors.firstName}
                    />
                    <Field
                      as={TextField}
                      name="lastName"
                      label="Last Name"
                      fullWidth
                      error={errors.lastName && touched.lastName}
                      helperText={touched.lastName && errors.lastName}
                    />
                  </Box>
                  
                  <Field
                    as={TextField}
                    name="email"
                    label="Email Address"
                    fullWidth
                    error={errors.email && touched.email}
                    helperText={touched.email && errors.email}
                  />
                  
                  <Field
                    as={TextField}
                    name="password"
                    label="Password"
                    type="password"
                    fullWidth
                    error={errors.password && touched.password}
                    helperText={touched.password && errors.password}
                  />
                  
                  <Field
                    as={TextField}
                    name="confirmPassword"
                    label="Confirm Password"
                    type="password"
                    fullWidth
                    error={errors.confirmPassword && touched.confirmPassword}
                    helperText={touched.confirmPassword && errors.confirmPassword}
                  />
                  
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
                    <Button 
                      onClick={handleBack}
                      disabled={isSubmitting}
                    >
                      Back
                    </Button>
                    <Button
                      type="submit"
                      variant="contained"
                      color="primary"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? <CircularProgress size={24} /> : 'Register Tenant'}
                    </Button>
                  </Box>
                </Box>
              </Form>
            )}
          </Formik>
        )}
        
        <Box sx={{ mt: 3, textAlign: 'center' }}>
          <Typography variant="body2">
            Already have an account?{' '}
            <MuiLink component={Link} to="/login" underline="hover">
              Sign in
            </MuiLink>
          </Typography>
        </Box>
      </Paper>
    </Container>
  );
};

export default TenantRegistration; 