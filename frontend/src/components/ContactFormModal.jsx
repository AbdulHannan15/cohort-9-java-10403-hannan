import React, { useEffect, useState } from 'react';

const TITLES = ['MR', 'MRS', 'MS', 'DR', 'PROF'];
const EMAIL_TYPES = ['WORK', 'PERSONAL', 'EDUCATION'];
const PHONE_TYPES = ['PERSONAL', 'OFFICE', 'HOME', 'WHATSAPP_ONLY', 'CALL_ONLY', 'EMERGENCY_ONLY'];

const emptyContact = {
  firstName: '',
  lastName: '',
  title: 'MR',
  emails: [{ email: '', type: 'PERSONAL' }],
  numbers: [{ number: '', numberType: 'PERSONAL' }],
};

export default function ContactFormModal({ initialContact, onSave, onCancel, saving, error }) {
  const [form, setForm] = useState(emptyContact);

  useEffect(() => {
    if (initialContact) {
      setForm({
        firstName: initialContact.firstName || '',
        lastName: initialContact.lastName || '',
        title: initialContact.title || 'MR',
        emails: initialContact.emails?.length
          ? initialContact.emails.map((e) => ({ email: e.email, type: e.type }))
          : [{ email: '', type: 'PERSONAL' }],
        numbers: initialContact.numbers?.length
          ? initialContact.numbers.map((n) => ({ number: n.number, numberType: n.numberType }))
          : [{ number: '', numberType: 'PERSONAL' }],
      });
    } else {
      setForm(emptyContact);
    }
  }, [initialContact]);

  const updateField = (field, value) => setForm((f) => ({ ...f, [field]: value }));

  const updateEmail = (idx, field, value) => {
    const emails = [...form.emails];
    emails[idx] = { ...emails[idx], [field]: value };
    setForm((f) => ({ ...f, emails }));
  };

  const addEmail = () => setForm((f) => ({ ...f, emails: [...f.emails, { email: '', type: 'PERSONAL' }] }));
  const removeEmail = (idx) => setForm((f) => ({ ...f, emails: f.emails.filter((_, i) => i !== idx) }));

  const updateNumber = (idx, field, value) => {
    const numbers = [...form.numbers];
    numbers[idx] = { ...numbers[idx], [field]: value };
    setForm((f) => ({ ...f, numbers }));
  };

  const addNumber = () => setForm((f) => ({ ...f, numbers: [...f.numbers, { number: '', numberType: 'PERSONAL' }] }));
  const removeNumber = (idx) => setForm((f) => ({ ...f, numbers: f.numbers.filter((_, i) => i !== idx) }));

  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      emails: form.emails.filter((e) => e.email.trim() !== ''),
      numbers: form.numbers.filter((n) => n.number.trim() !== ''),
    };
    onSave(payload);
  };

  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{initialContact ? 'Update Contact' : 'Create Contact'}</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <label>
              Title
              <select value={form.title} onChange={(e) => updateField('title', e.target.value)}>
                {TITLES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </label>
          </div>

          <div className="form-row two-col">
            <label>
              First Name
              <input
                type="text"
                value={form.firstName}
                onChange={(e) => updateField('firstName', e.target.value)}
                required
              />
            </label>
            <label>
              Last Name
              <input
                type="text"
                value={form.lastName}
                onChange={(e) => updateField('lastName', e.target.value)}
                required
              />
            </label>
          </div>

          <fieldset>
            <legend>Email Addresses</legend>
            {form.emails.map((email, idx) => (
              <div className="form-row two-col-with-action" key={idx}>
                <input
                  type="email"
                  placeholder="email@example.com"
                  value={email.email}
                  onChange={(e) => updateEmail(idx, 'email', e.target.value)}
                />
                <select value={email.type} onChange={(e) => updateEmail(idx, 'type', e.target.value)}>
                  {EMAIL_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                <button type="button" className="btn-remove" onClick={() => removeEmail(idx)}>&times;</button>
              </div>
            ))}
            <button type="button" className="btn-add" onClick={addEmail}>+ Add email</button>
          </fieldset>

          <fieldset>
            <legend>Phone Numbers</legend>
            {form.numbers.map((number, idx) => (
              <div className="form-row two-col-with-action" key={idx}>
                <input
                  type="text"
                  placeholder="+1 555 123 4567"
                  value={number.number}
                  onChange={(e) => updateNumber(idx, 'number', e.target.value)}
                />
                <select value={number.numberType} onChange={(e) => updateNumber(idx, 'numberType', e.target.value)}>
                  {PHONE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
                <button type="button" className="btn-remove" onClick={() => removeNumber(idx)}>&times;</button>
              </div>
            ))}
            <button type="button" className="btn-add" onClick={addNumber}>+ Add phone number</button>
          </fieldset>

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={saving}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
