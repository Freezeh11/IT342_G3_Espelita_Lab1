import { useEffect, useState } from 'react';
import { getCurrentUser } from '../services/authService';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const auth = localStorage.getItem('auth');
        if (!auth) {
            navigate('/login');
            return;
        }

        getCurrentUser(auth)
            .then(res => setUser(res.data))
            .catch(() => {
                localStorage.removeItem('auth');
                navigate('/login');
            });
    }, [navigate]);

    const handleLogout = () => {
        localStorage.removeItem('auth');
        localStorage.removeItem('user');
        navigate('/login');
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="container">
            <h2>Dashboard</h2>
            <p>Welcome, {user.username}!</p>
            <p>Role: {user.role}</p>
            <p>User ID: {user.id}</p>
            <button onClick={handleLogout}>Logout</button>
        </div>
    );
};

export default Dashboard;
