import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DeleteConfirmModal from '../DeleteConfirmModal';

const contact = { id: 1, firstName: 'Jane', lastName: 'Doe' };

describe('DeleteConfirmModal', () => {
  it('shows the contact name in the confirmation message', () => {
    render(<DeleteConfirmModal contact={contact} onConfirm={vi.fn()} onCancel={vi.fn()} deleting={false} />);

    expect(screen.getByText(/jane doe/i)).toBeInTheDocument();
  });

  it('calls onConfirm when the confirm button is clicked', async () => {
    const onConfirm = vi.fn();
    const user = userEvent.setup();
    render(<DeleteConfirmModal contact={contact} onConfirm={onConfirm} onCancel={vi.fn()} deleting={false} />);

    await user.click(screen.getByRole('button', { name: /confirm delete/i }));

    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it('calls onCancel when the cancel button is clicked', async () => {
    const onCancel = vi.fn();
    const user = userEvent.setup();
    render(<DeleteConfirmModal contact={contact} onConfirm={vi.fn()} onCancel={onCancel} deleting={false} />);

    await user.click(screen.getByRole('button', { name: /cancel/i }));

    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it('disables both buttons while deleting', () => {
    render(<DeleteConfirmModal contact={contact} onConfirm={vi.fn()} onCancel={vi.fn()} deleting={true} />);

    expect(screen.getByRole('button', { name: /deleting/i })).toBeDisabled();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
  });
});
