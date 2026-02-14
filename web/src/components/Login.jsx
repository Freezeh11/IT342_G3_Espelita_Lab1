import { useState } from 'react';
import { login } from '../services/authService';
import { useNavigate } from 'react-router-dom';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            await login(username, password);
            // In a real app, we'd handle the token/session here.
            // For this basic setup, we'll assume success means we can proceed.
            // But verified by /me call in Dashboard usually.
            // For Basic Auth, we need to store credentials.
            const authHeader = 'Basic ' + window.btoa(username + ':' + password);
            localStorage.setItem('auth', authHeader);
            localStorage.setItem('user', username);
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid credentials');
        }
    };

    // Helper for base64 encoding if btoa isn't available or for clarity
    const list_btoa = (str) => window.btoa(str);

    return (
        <div className="container">
            <h2>Login</h2>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <form onSubmit={handleLogin}>
                <div>
                    <label>Username:</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                </div>
                <div>
                    <label>Password:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button type="submit">Login</button>
            </form>
        </div>
    );
};

export default Login;
