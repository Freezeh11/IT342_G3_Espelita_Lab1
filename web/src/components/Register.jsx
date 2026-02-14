import React, { useState } from 'react';
import { register } from '../services/authService';
import { useNavigate } from 'react-router-dom';

const Register = () => {
    const [form, setForm] = useState({ username: '', email: '', password: '' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await register(form.username, form.email, form.password);
            alert("Registration Successful!");
            navigate('/login');
        } catch (err) { alert("Error registering user"); }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Register</h2>
            <input type="text" placeholder="Username" onChange={e => setForm({...form, username: e.target.value})} />
            <input type="email" placeholder="Email" onChange={e => setForm({...form, email: e.target.value})} />
            <input type="password" placeholder="Password" onChange={e => setForm({...form, password: e.target.value})} />
            <button type="submit">Create Account</button>
        </form>
    );
};

export default Register;