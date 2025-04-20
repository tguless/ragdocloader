import React from 'react';
import { Link } from 'react-router-dom';
import {
  Box,
  Button,
  Container,
  Typography,
  Paper,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Divider,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  Analytics,
  Storage,
  Security,
  Timeline,
  FindInPage,
  CloudUpload
} from '@mui/icons-material';

const FeatureCard = ({ icon, title, description }) => {
  return (
    <Card sx={{ 
      height: '100%', 
      display: 'flex', 
      flexDirection: 'column',
      transition: 'transform 0.3s, box-shadow 0.3s',
      '&:hover': {
        transform: 'translateY(-5px)',
        boxShadow: 5
      }
    }}>
      <CardContent sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
        <Box sx={{ 
          mb: 2,
          p: 2,
          borderRadius: '50%',
          bgcolor: 'primary.light',
          color: 'primary.contrastText',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          {icon}
        </Box>
        <Typography variant="h6" component="h3" gutterBottom>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
      </CardContent>
    </Card>
  );
};

const LandingPage = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  return (
    <Box>
      {/* Hero Section */}
      <Box 
        sx={{ 
          background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
          color: 'white',
          py: { xs: 8, md: 12 },
          mb: 6
        }}
      >
        <Container maxWidth="lg">
          <Grid container spacing={4} alignItems="center">
            <Grid item xs={12} md={6}>
              <Typography variant="h2" component="h1" gutterBottom fontWeight="bold">
                DocLoader
              </Typography>
              <Typography variant="h5" component="h2" sx={{ mb: 4 }}>
                Unlock the hidden connections in your document repository
              </Typography>
              <Typography variant="body1" paragraph sx={{ mb: 4 }}>
                Our AI-powered platform automatically processes, analyzes, and connects your documents, 
                revealing relationships and insights that would otherwise remain hidden.
              </Typography>
              <Button 
                component={Link} 
                to="/login" 
                variant="contained" 
                color="secondary" 
                size="large"
                sx={{ mr: 2, fontWeight: 'bold', px: 4 }}
              >
                Get Started
              </Button>
              <Button 
                component={Link} 
                to="/register-tenant" 
                variant="outlined" 
                color="inherit" 
                size="large"
                sx={{ fontWeight: 'bold', px: 4 }}
              >
                Register
              </Button>
            </Grid>
            <Grid item xs={12} md={6} sx={{ display: { xs: 'none', md: 'block' } }}>
              <Box 
                component="img"
                src="/document-network.svg" 
                alt="Document Network Visualization"
                sx={{ 
                  width: '100%',
                  maxHeight: 400,
                  objectFit: 'contain'
                }}
              />
            </Grid>
          </Grid>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg" sx={{ mb: 8 }}>
        <Box sx={{ mb: 6, textAlign: 'center' }}>
          <Typography variant="h4" component="h2" gutterBottom>
            Key Features
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 700, mx: 'auto' }}>
            DocLoader transforms your document management with powerful features designed to 
            reveal insights and connections across your entire document repository.
          </Typography>
        </Box>
        
        <Grid container spacing={4}>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<FindInPage fontSize="large" />}
              title="Smart Document Processing"
              description="Automatically extract and index content from multiple document formats, making information instantly searchable."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<Analytics fontSize="large" />}
              title="AI-Powered Embeddings"
              description="Transform documents into rich vector embeddings for semantic search and analysis beyond keyword matching."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<Timeline fontSize="large" />}
              title="Relationship Discovery"
              description="Automatically identify connections between documents through semantic similarity and reference analysis."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<Storage fontSize="large" />}
              title="Multi-Tenant Architecture"
              description="Enterprise-ready with isolated tenant databases for maximum security and customization."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<CloudUpload fontSize="large" />}
              title="Cloud Storage Integration"
              description="Connect directly to your existing S3 storage infrastructure without migrating your documents."
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FeatureCard 
              icon={<Security fontSize="large" />}
              title="Enterprise Security"
              description="Keep sensitive documents secure with role-based access control and encrypted data storage."
            />
          </Grid>
        </Grid>
      </Container>

      {/* How It Works Section */}
      <Box sx={{ bgcolor: 'grey.100', py: 8, mb: 8 }}>
        <Container maxWidth="lg">
          <Typography variant="h4" component="h2" gutterBottom align="center" sx={{ mb: 6 }}>
            How DocLoader Works
          </Typography>
          
          <Paper 
            elevation={3} 
            sx={{ 
              p: 4, 
              display: 'flex',
              flexDirection: isMobile ? 'column' : 'row',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}
          >
            <Box sx={{ 
              width: isMobile ? '100%' : '55%', 
              mb: isMobile ? 4 : 0,
              pr: isMobile ? 0 : 4
            }}>
              <Typography variant="h6" component="h3" gutterBottom>
                1. Connect Your Storage
              </Typography>
              <Typography variant="body1" paragraph>
                Link DocLoader to your S3 storage bucket or upload documents directly.
              </Typography>
              
              <Typography variant="h6" component="h3" gutterBottom>
                2. Create Processing Jobs
              </Typography>
              <Typography variant="body1" paragraph>
                Schedule when and how your documents are processed, with options for immediate or batched processing.
              </Typography>
              
              <Typography variant="h6" component="h3" gutterBottom>
                3. AI-Powered Analysis
              </Typography>
              <Typography variant="body1" paragraph>
                Our system extracts content, creates vector embeddings, and identifies relationships between documents.
              </Typography>
              
              <Typography variant="h6" component="h3" gutterBottom>
                4. Explore and Discover
              </Typography>
              <Typography variant="body1">
                Search semantically, visualize document networks, and uncover hidden connections in your data.
              </Typography>
            </Box>
            
            <Box 
              component="img"
              src="/workflow-diagram.svg"
              alt="DocLoader Workflow"
              sx={{ 
                width: isMobile ? '100%' : '45%',
                maxHeight: 350,
                objectFit: 'contain'
              }}
            />
          </Paper>
        </Container>
      </Box>

      {/* Footer CTA */}
      <Container maxWidth="md" sx={{ textAlign: 'center', mb: 8 }}>
        <Typography variant="h4" component="h2" gutterBottom>
          Ready to transform your document management?
        </Typography>
        <Typography variant="body1" paragraph sx={{ mb: 4 }}>
          Join organizations that are discovering new insights in their document repositories with DocLoader.
        </Typography>
        <Button 
          component={Link} 
          to="/login" 
          variant="contained" 
          color="primary" 
          size="large"
          sx={{ px: 6, py: 1.5, fontWeight: 'bold' }}
        >
          Get Started Now
        </Button>
      </Container>
    </Box>
  );
};

export default LandingPage; 