import { useEffect, useState } from 'react';
import { getCurrentUser } from '../services/authService';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
    const [user, setUser] = useState(null);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
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

    const styles = {
        pageWrapper: {
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100vw',
            height: '100vh',
            background: 'radial-gradient(circle at top right, #130a24 0%, #05010d 100%)',
            color: '#ffffff',
            fontFamily: "'Inter', sans-serif",
            overflowX: 'hidden',
        },
        navbar: {
            height: '70px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 40px',
            borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
            backgroundColor: 'rgba(5, 1, 13, 0.5)',
            backdropFilter: 'blur(10px)',
            position: 'sticky',
            top: 0,
            zIndex: 1000,
        },
        logo: {
            fontSize: '22px',
            fontWeight: '800',
            letterSpacing: '-0.02em',
        },
        purpleText: {
            color: '#9d80ff',
        },
        profileArea: {
            position: 'relative',
        },
        profileButton: {
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            background: 'rgba(255, 255, 255, 0.05)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            padding: '8px 16px',
            borderRadius: '100px',
            cursor: 'pointer',
            color: 'white',
            fontWeight: '500',
        },
        dropdown: {
            position: 'absolute',
            top: '50px',
            right: '0',
            width: '180px',
            backgroundColor: '#0d0a1a',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            borderRadius: '12px',
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)',
            overflow: 'hidden',
            display: isDropdownOpen ? 'block' : 'none',
        },
        dropdownItem: {
            padding: '12px 16px',
            fontSize: '14px',
            cursor: 'pointer',
            transition: 'background 0.2s',
            textAlign: 'left',
            color: '#8a8891',
        },
        mainContent: {
            padding: '60px 40px',
            maxWidth: '1000px',
            margin: '0 auto',
        },
        card: {
            backgroundColor: 'rgba(255, 255, 255, 0.03)',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            borderRadius: '24px',
            padding: '40px',
            backdropFilter: 'blur(12px)',
        }
    };

    if (!user) return <div style={styles.pageWrapper}><p style={{padding: '40px'}}>Scanning the cosmos...</p></div>;

    return (
        <div style={styles.pageWrapper}>
            <nav style={styles.navbar}>
                <div style={styles.logo}>
                    StandUp<span style={styles.purpleText}>-Sync</span>
                </div>
                
                <div style={styles.profileArea}>
                    <button 
                        style={styles.profileButton} 
                        onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    >
                        <div style={{width: '24px', height: '24px', borderRadius: '50%', backgroundColor: '#9d80ff', fontSize: '12px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#000'}}>
                            {user.username.charAt(0).toUpperCase()}
                        </div>
                        {user.username}
                    </button>

                    <div style={styles.dropdown}>
                        <div style={styles.dropdownItem} onClick={() => navigate('/profile')}>Profile Settings</div>
                        <div 
                            style={{...styles.dropdownItem, color: '#ff5959', borderTop: '1px solid rgba(255, 255, 255, 0.05)'}} 
                            onClick={handleLogout}
                        >
                            Logout
                        </div>
                    </div>
                </div>
            </nav>

            <main style={styles.mainContent}>
                <div style={styles.card}>
                    <h2 style={{fontSize: '32px', marginBottom: '20px'}}>Dashboard</h2>
                    <p style={{color: '#8a8891', marginBottom: '40px'}}>Welcome to the dashboard!</p>
                    
                    <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px'}}>
                        <div style={{padding: '20px', background: 'rgba(255,255,255,0.02)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)'}}>
                            <span style={{fontSize: '12px', color: '#9d80ff', fontWeight: 'bold', textTransform: 'uppercase'}}>Current Role</span>
                            <div style={{fontSize: '20px', marginTop: '5px'}}>{user.role}</div>
                        </div>
                        <div style={{padding: '20px', background: 'rgba(255,255,255,0.02)', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)'}}>
                            <span style={{fontSize: '12px', color: '#9d80ff', fontWeight: 'bold', textTransform: 'uppercase'}}>Identity ID</span>
                            <div style={{fontSize: '20px', marginTop: '5px'}}>#{user.id}</div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Dashboard;