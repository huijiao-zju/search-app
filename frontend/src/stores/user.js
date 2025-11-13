import { defineStore } from 'pinia';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    currentUser: (state) => state.user,
  },
  
  actions: {
    async login(username, password) {
      try {
        const response = await axios.post(`${API_URL}/auth/login`, {
          username,
          password,
        });
        // Backend returns JwtResponse with fields: token, type, id, username, email
        // Build a user object if "user" is not present for compatibility
        const { token } = response.data;
        const user = response.data.user || {
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
        };

        this.token = token;
        this.user = user;
        
        localStorage.setItem('token', token);
        
        // Configure axios to use the token for future requests
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        return { success: true };
      } catch (error) {
        console.error('Login error:', error);
        return { 
          success: false, 
          message: error.response?.data?.message || 'Login failed' 
        };
      }
    },
    
    async register(username, password, email) {
      try {
        const response = await axios.post(`${API_URL}/auth/register`, {
          username,
          password,
          email,
        });
        
        return { success: true, message: response.data.message };
      } catch (error) {
        console.error('Registration error:', error);
        return { 
          success: false, 
          message: error.response?.data?.message || 'Registration failed' 
        };
      }
    },
    
    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('token');
      delete axios.defaults.headers.common['Authorization'];
    },
    
    async fetchUserProfile() {
      if (!this.token) return;
      
      try {
        const response = await axios.get(`${API_URL}/user/profile`);
        this.user = response.data;
      } catch (error) {
        console.error('Error fetching user profile:', error);
        if (error.response?.status === 401) {
          // Token expired or invalid
          this.logout();
        }
      }
    },
  },
}); 

// Global Axios response interceptor for 401 handling
axios.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error?.response?.status === 401) {
      try {
        const store = useUserStore();
        store.logout();
      } catch (_) {}
    }
    return Promise.reject(error);
  }
);
