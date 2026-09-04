import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import LoginPage from '../LoginPage';

const mockLogin = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

function renderLoginPage() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );
}

describe('LoginPage', () => {
  beforeEach(() => {
    mockLogin.mockReset();
    mockNavigate.mockReset();
  });

  it('renders the identifier and password fields', () => {
    renderLoginPage();

    expect(screen.getByLabelText(/email or phone/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
  });

  it('calls login with the entered credentials and navigates to /contacts on success', async () => {
    mockLogin.mockResolvedValueOnce({ id: 1, loginIdentifier: 'jane@example.com' });
    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText(/email or phone/i), 'jane@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('jane@example.com', 'password123');
      expect(mockNavigate).toHaveBeenCalledWith('/contacts');
    });
  });

  it('shows an error message when login fails', async () => {
    mockLogin.mockRejectedValueOnce({
      response: { data: { message: 'Invalid login credentials' } },
    });
    const user = userEvent.setup();
    renderLoginPage();

    await user.type(screen.getByLabelText(/email or phone/i), 'jane@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'wrong-password');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByText(/invalid login credentials/i)).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
