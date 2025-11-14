const API_BASE_URL = 'http://localhost:8080/api/frontend';

class ApiService {
  private async request(endpoint: string, options: RequestInit = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultOptions: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Importante para las sesiones
      ...options,
    };

    try {
      console.log('Making request to:', url, defaultOptions);
      
      const response = await fetch(url, defaultOptions);
      
      console.log('Response received:', response.status, response.statusText);
      
      if (!response.ok) {
        const error = await response.json().catch(() => ({ 
          message: `HTTP error! status: ${response.status}` 
        }));
        console.error('API Error:', error);
        throw new Error(error.message || `HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Response data:', data);
      return data;
    } catch (fetchError) {
      console.error('Fetch Error:', fetchError);
      throw fetchError;
    }
  }

  // Autenticación
  async login(credentials: { username: string; password: string }) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
  }

  async logout() {
    return this.request('/auth/logout', {
      method: 'POST',
    });
  }

  async getCurrentUser() {
    return this.request('/auth/me');
  }

  // Vehículos
  async getVehicles() {
    console.log('🌐 ApiService: Making request to get vehicles...');
    const result = await this.request('/vehicles');
    console.log('🌐 ApiService: Vehicle response received:', result);
    return result;
  }

  async getVehicle(id: string) {
    return this.request(`/vehicles/${id}`);
  }

  async createVehicle(vehicle: { placa: string; modelo: string; capacidad: number; estado: string }) {
    return this.request('/vehicles', {
      method: 'POST',
      body: JSON.stringify(vehicle),
    });
  }

  async updateVehicle(id: string, vehicle: { placa: string; modelo: string; capacidad: number; estado: string }) {
    return this.request(`/vehicles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(vehicle),
    });
  }

  async deleteVehicle(id: string) {
    return this.request(`/vehicles/${id}`, {
      method: 'DELETE',
    });
  }
}

export const apiService = new ApiService();
export default apiService;