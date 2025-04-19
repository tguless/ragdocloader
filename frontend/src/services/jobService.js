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
      const response = await api.get(`/jobs/${jobId}/documents`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching documents for job ${jobId}:`, error);
      throw error;
    }
  }
};

export default jobService; 