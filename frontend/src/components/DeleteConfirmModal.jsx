import React from 'react';

export default function DeleteConfirmModal({ contact, onConfirm, onCancel, deleting }) {
  return (
    <div className="modal-overlay">
      <div className="modal modal-small">
        <h2>Delete Contact</h2>
        <p>
          Are you sure you want to delete{' '}
          <strong>{contact.firstName} {contact.lastName}</strong>? This cannot be undone.
        </p>
        <div className="modal-actions">
          <button className="btn btn-secondary" onClick={onCancel} disabled={deleting}>
            Cancel
          </button>
          <button className="btn btn-danger" onClick={onConfirm} disabled={deleting}>
            {deleting ? 'Deleting...' : 'Confirm Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}
