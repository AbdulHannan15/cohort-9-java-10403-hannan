import React, { useEffect, useState, useCallback } from 'react';
import Navbar from '../components/Navbar';
import ContactFormModal from '../components/ContactFormModal';
import DeleteConfirmModal from '../components/DeleteConfirmModal';
import { getContacts, createContact, updateContact, deleteContact } from '../api/contactApi';

const PAGE_SIZE = 10;

export default function ContactsPage() {
  const [contacts, setContacts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState('');

  const [showFormModal, setShowFormModal] = useState(false);
  const [editingContact, setEditingContact] = useState(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');

  const [deletingContact, setDeletingContact] = useState(null);
  const [deleteInFlight, setDeleteInFlight] = useState(false);

  const loadContacts = useCallback(async (pageToLoad, searchTerm) => {
    setLoading(true);
    setListError('');
    try {
      const data = await getContacts(pageToLoad, PAGE_SIZE, searchTerm);
      setContacts(data.content || []);
      setTotalPages(data.totalPages ?? 0);
      setTotalElements(data.totalElements ?? 0);
    } catch (err) {
      setListError(err.response?.data?.message || 'Failed to load contacts');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadContacts(page, search);
  }, [page, loadContacts]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    loadContacts(0, search);
  };

  const openCreateModal = () => {
    setEditingContact(null);
    setFormError('');
    setShowFormModal(true);
  };

  const openEditModal = (contact) => {
    setEditingContact(contact);
    setFormError('');
    setShowFormModal(true);
  };

  const handleSave = async (payload) => {
    setSaving(true);
    setFormError('');
    try {
      if (editingContact) {
        await updateContact(editingContact.id, payload);
      } else {
        await createContact(payload);
      }
      setShowFormModal(false);
      loadContacts(page, search);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save contact');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    setDeleteInFlight(true);
    try {
      await deleteContact(deletingContact.id);
      setDeletingContact(null);
      loadContacts(page, search);
    } catch (err) {
      setListError(err.response?.data?.message || 'Failed to delete contact');
    } finally {
      setDeleteInFlight(false);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="page-container">
        <div className="page-header">
          <h1>Contacts {totalElements > 0 && <span className="count-badge">{totalElements}</span>}</h1>
          <button className="btn btn-primary" onClick={openCreateModal}>+ New Contact</button>
        </div>

        <form className="search-bar" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            placeholder="Search by first or last name..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button type="submit" className="btn btn-secondary">Search</button>
        </form>

        {listError && <div className="alert alert-error">{listError}</div>}

        {loading ? (
          <div className="page-loading">Loading contacts...</div>
        ) : contacts.length === 0 ? (
          <div className="empty-state">No contacts found. Create your first contact to get started.</div>
        ) : (
          <table className="contact-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Title</th>
                <th>Emails</th>
                <th>Phone Numbers</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {contacts.map((c) => (
                <tr key={c.id}>
                  <td>{c.firstName} {c.lastName}</td>
                  <td>{c.title || '-'}</td>
                  <td>
                    {c.emails?.length
                      ? c.emails.map((e) => (
                          <div key={e.id || e.email} className="tag-line">
                            {e.email} <span className="tag">{e.type}</span>
                          </div>
                        ))
                      : '-'}
                  </td>
                  <td>
                    {c.numbers?.length
                      ? c.numbers.map((n) => (
                          <div key={n.id || n.number} className="tag-line">
                            {n.number} <span className="tag">{n.numberType}</span>
                          </div>
                        ))
                      : '-'}
                  </td>
                  <td className="row-actions">
                    <button className="btn-link" onClick={() => openEditModal(c)}>Edit</button>
                    <button className="btn-link btn-link-danger" onClick={() => setDeletingContact(c)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="btn btn-secondary"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>
            <span>Page {page + 1} of {totalPages}</span>
            <button
              className="btn btn-secondary"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        )}
      </div>

      {showFormModal && (
        <ContactFormModal
          initialContact={editingContact}
          onSave={handleSave}
          onCancel={() => setShowFormModal(false)}
          saving={saving}
          error={formError}
        />
      )}

      {deletingContact && (
        <DeleteConfirmModal
          contact={deletingContact}
          onConfirm={handleDeleteConfirm}
          onCancel={() => setDeletingContact(null)}
          deleting={deleteInFlight}
        />
      )}
    </div>
  );
}
