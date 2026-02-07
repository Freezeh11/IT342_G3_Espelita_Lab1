import React, { useEffect, useState } from 'react';
import { getMe } from '../services/authService';

const Dashboard = () => {
    const [user, setUser] = useState({ username: "Hello", email: "hello@gmail.com" });

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await getMe();
                setUser(response.data);
            } catch (err) {
                console.log("Not logged in, showing guest data");
                // Removed the logout() redirect so you can stay on the page
            }
        };
        fetchData();
    }, []);

    return (
        <div>
            <h2>Dashboard</h2>
            <p>Welcome, <strong>{user.username}</strong>!</p>
            <p>Email: {user.email}</p>
        </div>
    );
};

export default Dashboard;