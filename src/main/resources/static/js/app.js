const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('jwt_token');
}

function setAuth(token, user) {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('user', JSON.stringify(user));
}

function clearAuth() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
    window.location.href = '/login.html';
}

function checkAuth() {
    if (!getToken()) {
        window.location.href = '/login.html';
    }
}

async function apiRequest(endpoint, options = {}) {
    const token = getToken();
    const headers = options.headers || {};
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    if (options.body && typeof options.body === 'object') {
        headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(options.body);
    }
    
    options.headers = headers;

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        
        if (response.status === 401) {
            clearAuth();
            return;
        }
        
        if (!response.ok) {
            throw new Error(data.message || 'API request failed');
        }
        
        return data;
    } catch (err) {
        throw err;
    }
}

function generateIdempotencyKey() {
    return 'idemp-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
}
