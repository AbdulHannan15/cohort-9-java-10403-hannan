import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ContactFormModal from '../ContactFormModal';

describe('ContactFormModal', () => {
  it('renders in create mode with one empty email and phone row by default', () => {
    render(<ContactFormModal initialContact={null} onSave={vi.fn()} onCancel={vi.fn()} saving={false} error="" />);

    expect(screen.getByText(/create contact/i)).toBeInTheDocument();
    expect(screen.getAllByPlaceholderText(/email@example.com/i)).toHaveLength(1);
    expect(screen.getAllByPlaceholderText(/\+1 555/i)).toHaveLength(1);
  });

  it('pre-populates fields when editing an existing contact', () => {
    const contact = {
      firstName: 'Jane',
      lastName: 'Doe',
      title: 'MS',
      emails: [{ id: 1, email: 'jane@work.com', type: 'WORK' }],
      numbers: [{ id: 1, number: '+15551234567', numberType: 'OFFICE' }],
    };

    render(<ContactFormModal initialContact={contact} onSave={vi.fn()} onCancel={vi.fn()} saving={false} error="" />);

    expect(screen.getByText(/update contact/i)).toBeInTheDocument();
    expect(screen.getByDisplayValue('Jane')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Doe')).toBeInTheDocument();
    expect(screen.getByDisplayValue('jane@work.com')).toBeInTheDocument();
    expect(screen.getByDisplayValue('+15551234567')).toBeInTheDocument();
  });

  it('adds another email row when "Add email" is clicked', async () => {
    const user = userEvent.setup();
    render(<ContactFormModal initialContact={null} onSave={vi.fn()} onCancel={vi.fn()} saving={false} error="" />);

    await user.click(screen.getByRole('button', { name: /\+ add email/i }));

    expect(screen.getAllByPlaceholderText(/email@example.com/i)).toHaveLength(2);
  });

  it('strips blank email/number rows and calls onSave with the filled fields', async () => {
    const onSave = vi.fn();
    const user = userEvent.setup();
    render(<ContactFormModal initialContact={null} onSave={onSave} onCancel={vi.fn()} saving={false} error="" />);

    await user.type(screen.getByLabelText(/first name/i), 'Jane');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByPlaceholderText(/email@example.com/i), 'jane@work.com');
    await user.click(screen.getByRole('button', { name: /^save$/i }));

    expect(onSave).toHaveBeenCalledTimes(1);
    const payload = onSave.mock.calls[0][0];
    expect(payload.firstName).toBe('Jane');
    expect(payload.lastName).toBe('Doe');
    expect(payload.emails).toEqual([{ email: 'jane@work.com', type: 'PERSONAL' }]);
    expect(payload.numbers).toEqual([]); // blank phone row was stripped
  });

  it('calls onCancel when Cancel is clicked', async () => {
    const onCancel = vi.fn();
    const user = userEvent.setup();
    render(<ContactFormModal initialContact={null} onSave={vi.fn()} onCancel={onCancel} saving={false} error="" />);

    await user.click(screen.getByRole('button', { name: /cancel/i }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('shows the error message when provided', () => {
    render(
      <ContactFormModal
        initialContact={null}
        onSave={vi.fn()}
        onCancel={vi.fn()}
        saving={false}
        error="Email already in use"
      />
    );

    expect(screen.getByText(/email already in use/i)).toBeInTheDocument();
  });
});
