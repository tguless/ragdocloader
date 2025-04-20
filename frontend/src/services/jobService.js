import api from './axiosConfig';

const jobService = {
  // Get all jobs for current user
  getAllJobs: async () => {
    try {
      const response = await api.get('/jobs');
      return response.data;
    } catch (error) {
      console.error('Error fetching jobs:', error);
      throw error;
    }
  },

  // Get job by ID
  getJobById: async (jobId) => {
    try {
      const response = await api.get(`/jobs/${jobId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching job ${jobId}:`, error);
      throw error;
    }
  },

  // Create a new document job
  createJob: async (jobData) => {
    try {
      // Format the job data to match the backend DTO
      // If S3 source type, ensure we have bucketId and path
      if (jobData.sourceType === 's3') {
        jobData = {
          name: jobData.name,
          description: jobData.description,
          type: jobData.type,
          config: jobData.config || {},
          sourceType: 's3',
          s3BucketId: jobData.s3BucketId,
          s3SourcePath: jobData.s3SourcePath || '/',
          scheduledTime: jobData.scheduledTime
        };
      } 
      // If upload source type, process the document list
      else if (jobData.sourceType === 'upload' && jobData.files && jobData.files.length > 0) {
        // First upload the documents
        const formData = new FormData();
        jobData.files.forEach(file => {
          formData.append('files', file);
        });
        
        // Upload the files
        const uploadResponse = await api.post('/documents/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        // Get the document IDs from the response
        const documentIds = uploadResponse.data.documentIds;
        
        // Create job with document IDs
        jobData = {
          name: jobData.name,
          description: jobData.description,
          type: jobData.type,
          config: jobData.config || {},
          sourceType: 'upload',
          documentIds: documentIds,
          scheduledTime: jobData.scheduledTime
        };
      }
      
      const response = await api.post('/jobs', jobData);
      return response.data;
    } catch (error) {
      console.error('Error creating job:', error);
      throw error;
    }
  },

  // Update a job
  updateJob: async (jobId, jobData) => {
    try {
      // Format the job data to match the backend DTO (similar to createJob)
      if (jobData.sourceType === 's3') {
        jobData = {
          name: jobData.name,
          description: jobData.description,
          type: jobData.type,
          config: jobData.config || {},
          sourceType: 's3',
          s3BucketId: jobData.s3BucketId,
          s3SourcePath: jobData.s3SourcePath || '/',
          scheduledTime: jobData.scheduledTime
        };
      } 
      // If upload source type, process the document list
      else if (jobData.sourceType === 'upload' && jobData.files && jobData.files.length > 0) {
        // First upload any new documents
        const newFiles = jobData.files.filter(file => !(file instanceof String || typeof file === 'string'));
        
        if (newFiles.length > 0) {
          const formData = new FormData();
          newFiles.forEach(file => {
            formData.append('files', file);
          });
          
          // Upload the files
          const uploadResponse = await api.post('/documents/upload', formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });
          
          // Get the document IDs from the response
          const newDocumentIds = uploadResponse.data.documentIds;
          
          // Combine with existing document IDs
          const existingDocumentIds = jobData.documentIds || [];
          const allDocumentIds = [...existingDocumentIds, ...newDocumentIds];
          
          // Update job with document IDs
          jobData = {
            name: jobData.name,
            description: jobData.description,
            type: jobData.type,
            config: jobData.config || {},
            sourceType: 'upload',
            documentIds: allDocumentIds,
            scheduledTime: jobData.scheduledTime
          };
        }
      }
      
      const response = await api.put(`/jobs/${jobId}`, jobData);
      return response.data;
    } catch (error) {
      console.error(`Error updating job ${jobId}:`, error);
      throw error;
    }
  },

  // Delete a job
  deleteJob: async (jobId) => {
    try {
      await api.delete(`/jobs/${jobId}`);
      return true;
    } catch (error) {
      console.error(`Error deleting job ${jobId}:`, error);
      throw error;
    }
  },

  // Get job status statistics (counts by status)
  getJobStats: async () => {
    try {
      const response = await api.get('/jobs/stats');
      return response.data;
    } catch (error) {
      console.error('Error fetching job statistics:', error);
      throw error;
    }
  },

  // Get job documents
  getJobDocuments: async (jobId) => {
    try {
      const response = await api.get(`/documents/job/${jobId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching documents for job ${jobId}:`, error);
      throw error;
    }
  }
};

export default jobService; 