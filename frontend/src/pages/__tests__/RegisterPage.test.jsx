import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import RegisterPage from '../RegisterPage';

const mockRegister = vi.fn();
const mockLogin = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ register: mockRegister, login: mockLogin }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

function renderRegisterPage() {
  return render(
    <MemoryRouter>
      <RegisterPage />
    </MemoryRouter>
  );
}

describe('RegisterPage', () => {
  beforeEach(() => {
    mockRegister.mockReset();
    mockLogin.mockReset();
    mockNavigate.mockReset();
  });

  it('blocks submission when passwords do not match', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    await user.type(screen.getByLabelText(/email or phone/i), 'jane@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'different');
    await user.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText(/passwords do not match/i)).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });

  it('registers then auto-logs in and navigates to /contacts', async () => {
    mockRegister.mockResolvedValueOnce({ id: 1, loginIdentifier: 'jane@example.com' });
    mockLogin.mockResolvedValueOnce({ id: 1, loginIdentifier: 'jane@example.com' });
    const user = userEvent.setup();
    renderRegisterPage();

    await user.type(screen.getByLabelText(/email or phone/i), 'jane@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith('jane@example.com', 'password123', undefined);
      expect(mockLogin).toHaveBeenCalledWith('jane@example.com', 'password123');
      expect(mockNavigate).toHaveBeenCalledWith('/contacts');
    });
  });

  it('shows a server error message (e.g. duplicate account) when registration fails', async () => {
    mockRegister.mockRejectedValueOnce({
      response: { data: { message: 'An account with this email/phone already exists' } },
    });
    const user = userEvent.setup();
    renderRegisterPage();

    await user.type(screen.getByLabelText(/email or phone/i), 'jane@example.com');
    await user.type(screen.getByLabelText(/^password$/i), 'password123');
    await user.type(screen.getByLabelText(/confirm password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /register/i }));

    expect(await screen.findByText(/already exists/i)).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
