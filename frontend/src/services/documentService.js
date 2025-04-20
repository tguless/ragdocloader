import api from './axiosConfig';

const documentService = {
  // Upload documents
  uploadDocuments: async (files) => {
    try {
      const formData = new FormData();
      files.forEach(file => {
        formData.append('files', file);
      });
      
      const response = await api.post('/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      return response.data;
    } catch (error) {
      console.error('Error uploading documents:', error);
      throw error;
    }
  },
  
  // Get all documents for current user
  getUserDocuments: async () => {
    try {
      const response = await api.get('/documents');
      return response.data;
    } catch (error) {
      console.error('Error fetching user documents:', error);
      throw error;
    }
  },
  
  // Get document by ID
  getDocumentById: async (documentId) => {
    try {
      const response = await api.get(`/documents/${documentId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching document ${documentId}:`, error);
      throw error;
    }
  },
  
  // Get documents for a job
  getDocumentsByJobId: async (jobId) => {
    try {
      const response = await api.get(`/documents/job/${jobId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching documents for job ${jobId}:`, error);
      throw error;
    }
  },
  
  // Delete a document
  deleteDocument: async (documentId) => {
    try {
      await api.delete(`/documents/${documentId}`);
      return true;
    } catch (error) {
      console.error(`Error deleting document ${documentId}:`, error);
      throw error;
    }
  }
};

export default documentService; 