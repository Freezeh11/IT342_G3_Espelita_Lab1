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
        setError('');
        try {
            await login(username, password);
            const authHeader = 'Basic ' + window.btoa(username + ':' + password);
            localStorage.setItem('auth', authHeader);
            localStorage.setItem('user', username);
            navigate('/dashboard');
        } catch (err) {
            setError('Invalid username or password');
        }
    };

    const styles = {
        pageWrapper: {
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100vw',
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'radial-gradient(circle at center, #130a24 0%, #05010d 100%)',
            fontFamily: "'Inter', sans-serif",
            margin: 0,
            padding: 0,
            zIndex: 9999
        },
        glassCard: {
            width: '90%',
            maxWidth: '420px',
            padding: '48px 40px',
            borderRadius: '28px',
            backgroundColor: 'rgba(10, 5, 20, 0.7)',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            backdropFilter: 'blur(16px)',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
            textAlign: 'center',
        },
        header: {
            fontSize: '32px',
            fontWeight: '800',
            color: '#ffffff',
            marginBottom: '8px',
            letterSpacing: '-0.03em',
        },
        purpleText: {
            color: '#9d80ff',
        },
        subtext: {
            color: '#8a8891',
            fontSize: '14px',
            marginBottom: '32px',
        },
        errorBox: {
            backgroundColor: 'rgba(255, 89, 89, 0.1)',
            color: '#ff5959',
            padding: '10px',
            borderRadius: '8px',
            fontSize: '13px',
            marginBottom: '20px',
            border: '1px solid rgba(255, 89, 89, 0.2)'
        },
        inputField: {
            width: '100%',
            padding: '14px 18px',
            marginBottom: '16px',
            borderRadius: '12px',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            backgroundColor: 'rgba(255, 255, 255, 0.04)',
            color: '#ffffff',
            fontSize: '16px',
            outline: 'none',
            boxSizing: 'border-box',
        },
        primaryButton: {
            width: '100%',
            padding: '16px',
            borderRadius: '12px',
            border: 'none',
            backgroundColor: '#ffffff', 
            color: '#000000',
            fontWeight: '700',
            fontSize: '16px',
            cursor: 'pointer',
            marginTop: '8px',
            transition: 'all 0.2s ease',
        },
        footerText: {
            marginTop: '28px',
            fontSize: '14px',
            color: '#8a8891',
        },
        link: {
            color: '#9d80ff',
            fontWeight: '600',
            marginLeft: '6px',
            cursor: 'pointer',
            textDecoration: 'none'
        }
    };

    return (
        <div style={styles.pageWrapper}>
            <div style={styles.glassCard}>
                <h1 style={styles.header}>
                    Welcome <span style={styles.purpleText}>Back</span>
                </h1>

                {error && <div style={styles.errorBox}>{error}</div>}
                
                <form onSubmit={handleLogin}>
                    <input 
                        style={styles.inputField} 
                        type="text" 
                        placeholder="Username" 
                        value={username}
                        onChange={(e) => setUsername(e.target.value)} 
                    />
                    <input 
                        style={styles.inputField} 
                        type="password" 
                        placeholder="Password" 
                        value={password}
                        onChange={(e) => setPassword(e.target.value)} 
                    />
                    
                    <button 
                        type="submit"
                        style={styles.primaryButton}
                        onMouseOver={(e) => {
                            e.target.style.transform = 'scale(1.02)';
                            e.target.style.backgroundColor = '#f1f1f1';
                        }}
                        onMouseOut={(e) => {
                            e.target.style.transform = 'scale(1)';
                            e.target.style.backgroundColor = '#ffffff';
                        }}
                    >
                        Login
                    </button>
                </form>

                <div style={styles.footerText}>
                    Don't have an account? 
                    <span style={styles.link} onClick={() => navigate('/register')}>Register</span>
                </div>
            </div>
        </div>
    );
};

export default Login;