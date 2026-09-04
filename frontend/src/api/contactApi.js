import axiosClient from './axiosClient';

export const getContacts = (page = 0, size = 10, search = '') => {
  const params = { page, size };
  if (search) params.search = search;
  return axiosClient.get('/contacts', { params }).then((res) => res.data);
};

export const getContact = (id) =>
  axiosClient.get(`/contacts/${id}`).then((res) => res.data);

export const createContact = (payload) =>
  axiosClient.post('/contacts', payload).then((res) => res.data);

export const updateContact = (id, payload) =>
  axiosClient.put(`/contacts/${id}`, payload).then((res) => res.data);

export const deleteContact = (id) =>
  axiosClient.delete(`/contacts/${id}`).then((res) => res.data);
