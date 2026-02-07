import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export const register = (username, email, password) => {
    return axios.post(`${API_URL}/auth/register`, { username, email, password });
};

export const login = (username, password) => {
    // We use Basic Auth for Session 1. This encodes credentials to Base64.
    const token = btoa(`${username}:${password}`);
    localStorage.setItem('userToken', token); // Store for protected routes
    return axios.post(`${API_URL}/auth/login`, {}, {
        headers: { 'Authorization': `Basic ${token}` }
    });
};

export const getMe = () => {
    const token = localStorage.getItem('userToken');
    return axios.get(`${API_URL}/user/me`, {
        headers: { 'Authorization': `Basic ${token}` }
    });
};

export const logout = () => {
    localStorage.removeItem('userToken');
    window.location.href = '/login';
};