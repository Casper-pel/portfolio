import { UrlaubsAntragDto } from "../model/urlaubsAntragDto";
import { API_BASE_URL } from "../requests/baseUrl";

export const UrlaubsAntragService = {
  async getAll() {
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/all`, {
      credentials: 'include'
    });
    if (response.status === 204) {
      return [];
    }
    return response.json();
  },

  async getByEmployeeId() {
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/user`, {
      credentials: 'include'
    });
    if (response.status === 204) {
      return [];
    }
    return response.json();
  },

  async create(antrag: UrlaubsAntragDto) {
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/add`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(antrag),
      credentials: 'include'
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Server error:', errorText);
      throw new Error(`Fehler ${response.status}: ${errorText}`);
    }
    
    if (response.status === 200) {
      const successMessage = await response.text();
      console.log('Success:', successMessage);
      return successMessage;
    }
    
    return response.json();
  },

  async update(antrag: UrlaubsAntragDto) {
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/update`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(antrag),
      credentials: 'include'
    });
    if (response.status === 204 || response.status === 201 || response.status === 200) {
      return [];
    }
    return response.json();
  },

  async delete(id: number) {
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/delete/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    if (response.status === 204 || response.status === 201 || response.status === 200) {
      return [];
    }
    return response.json();
  },

  async review(id: number, status: string, comment: string, reviewerId: number) {
    // Überprüfen von ID bevor die API aufgerufen wird
    if (!id || id === null || id === undefined) {
      throw new Error('Antrag ID is required for review');
    }
    
    const currentAntrag = await this.getById(id);
    
    const reviewData = {
      antragsId: id,
      employeeId: currentAntrag.employeeId,
      startDatum: currentAntrag.startDatum,
      endDatum: currentAntrag.endDatum,
      status: status,
      type: currentAntrag.type,
      grund: currentAntrag.grund,
      reviewDate: new Date().toISOString().split('T')[0],
      reviewerId: reviewerId,
      comment: comment || currentAntrag.comment
    };
    
    console.log('Sending review data:', reviewData);
    
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/review`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reviewData),
      credentials: 'include'
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Review failed:', errorText);
      throw new Error(`Failed to review antrag: ${errorText}`);
    }

    if (response.status === 204 || response.status === 201 || response.status === 200) {
      return [];
    }
    
    return response.json();
  },

  async getById(id: number) {
    if (!id || id === null || id === undefined) {
      throw new Error('Antrag ID is required');
    }
    
    const response = await fetch(`${API_BASE_URL}/urlaubsantrag/get/${id}`, {
      credentials: 'include'
    });
    if (!response.ok) {
      throw new Error('Failed to fetch antrag');
    }
    return response.json();
  }
};