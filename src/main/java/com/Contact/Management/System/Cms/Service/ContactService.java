package com.Contact.Management.System.Cms.Service;

import com.Contact.Management.System.Cms.DTO.ContactRequest;
import com.Contact.Management.System.Cms.DTO.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {

    ContactResponse createContact(Long userId, ContactRequest request);

    ContactResponse updateContact(Long userId, Long contactId, ContactRequest request);

    void deleteContact(Long userId, Long contactId);

    ContactResponse getContact(Long userId, Long contactId);

    Page<ContactResponse> getContacts(Long userId, Pageable pageable);

    Page<ContactResponse> searchContacts(Long userId, String keyword, Pageable pageable);
}
