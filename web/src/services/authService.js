import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth/';

export const register = async (username, email, password) => {
    return axios.post(API_URL + 'register', {
        username,
        email,
        password
    });
};

export const login = async (username, password) => {
    // For Basic Auth, we don't necessarily need a post login if we just use the header
    // But to match the user's flow or if we implemented a real login endpoint:
    return axios.post(API_URL + 'login', {
        username,
        password
    });
};

export const getCurrentUser = async (token) => {
    return axios.get(API_URL.replace('auth/', 'user/') + 'me', {
        headers: { 'Authorization': token }
    });
};
