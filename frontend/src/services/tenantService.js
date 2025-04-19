import api from './axiosConfig';

const tenantService = {
  // Get all tenants (admin only)
  getAllTenants: async () => {
    try {
      const response = await api.get('/tenants');
      return response.data;
    } catch (error) {
      console.error('Error fetching tenants:', error);
      throw error;
    }
  },

  // Get tenant by ID
  getTenantById: async (tenantId) => {
    try {
      const response = await api.get(`/tenants/${tenantId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching tenant ${tenantId}:`, error);
      throw error;
    }
  },

  // Create a new tenant
  createTenant: async (tenantData) => {
    try {
      const response = await api.post('/tenants', tenantData);
      return response.data;
    } catch (error) {
      console.error('Error creating tenant:', error);
      throw error;
    }
  },

  // Register a new tenant (public endpoint)
  registerTenant: async (tenantData) => {
    try {
      const response = await api.post('/tenants/register', tenantData);
      return response.data;
    } catch (error) {
      console.error('Error registering tenant:', error);
      throw error;
    }
  },

  // Update a tenant
  updateTenant: async (tenantId, tenantData) => {
    try {
      const response = await api.put(`/tenants/${tenantId}`, tenantData);
      return response.data;
    } catch (error) {
      console.error(`Error updating tenant ${tenantId}:`, error);
      throw error;
    }
  },

  // Delete a tenant
  deleteTenant: async (tenantId) => {
    try {
      await api.delete(`/tenants/${tenantId}`);
      return true;
    } catch (error) {
      console.error(`Error deleting tenant ${tenantId}:`, error);
      throw error;
    }
  }
};

export default tenantService; 