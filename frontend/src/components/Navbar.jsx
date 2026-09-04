import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">Contact Management System</div>
      <div className="navbar-links">
        <Link to="/contacts">Contacts</Link>
        <Link to="/profile">Profile</Link>
        <button className="btn-link" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
