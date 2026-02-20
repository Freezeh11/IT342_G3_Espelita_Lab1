import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
import { register } from '../services/authService'; 

const Register = () => {
    const navigate = useNavigate(); 
    const [form, setForm] = useState({ username: '', email: '', password: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await register(form.username, form.email, form.password);
            alert("Registration Successful!");
            navigate('/login');
        } catch (err) { 
            alert("Error registering user. Please try again."); 
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
            marginBottom: '36px',
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
            transition: 'transform 0.2s ease',
        },
        footerText: {
            marginTop: '28px',
            fontSize: '14px',
            color: '#8a8891',
        },
        loginLink: {
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
                    Regi<span style={styles.purpleText}>ster</span>
                </h1>
            
                <form onSubmit={handleSubmit}>
                    <input 
                        style={styles.inputField} 
                        type="text" 
                        placeholder="Username" 
                        required
                        onChange={e => setForm({...form, username: e.target.value})} 
                    />
                    <input 
                        style={styles.inputField} 
                        type="email" 
                        placeholder="Email" 
                        required
                        onChange={e => setForm({...form, email: e.target.value})} 
                    />
                    <input 
                        style={styles.inputField} 
                        type="password" 
                        placeholder="Password" 
                        required
                        onChange={e => setForm({...form, password: e.target.value})} 
                    />
                    
                    <button 
                        type="submit"
                        style={styles.primaryButton}
                        onMouseOver={(e) => e.target.style.transform = 'scale(1.02)'}
                        onMouseOut={(e) => e.target.style.transform = 'scale(1)'}
                    >
                        Create Account
                    </button>
                </form>

                <div style={styles.footerText}>
                    Already have an account? 
                    <span style={styles.loginLink} onClick={() => navigate('/login')}>Log in</span>
                </div>
            </div>
        </div>
    );
};

export default Register;