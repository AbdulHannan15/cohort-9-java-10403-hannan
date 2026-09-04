package com.Contact.Management.System.Cms.Service;

import com.Contact.Management.System.Cms.DTO.ContactRequest;
import com.Contact.Management.System.Cms.DTO.ContactResponse;
import com.Contact.Management.System.Cms.DTO.EmailDto;
import com.Contact.Management.System.Cms.DTO.PhoneNumberDto;
import com.Contact.Management.System.Cms.Entity.Contact;
import com.Contact.Management.System.Cms.Entity.EmailEntity;
import com.Contact.Management.System.Cms.Entity.PhoneNumberEntity;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Exception.DuplicateResourceException;
import com.Contact.Management.System.Cms.Exception.ResourceNotFoundException;
import com.Contact.Management.System.Cms.Repo.ContactRepo;
import com.Contact.Management.System.Cms.Repo.EmailRepo;
import com.Contact.Management.System.Cms.Repo.NumberRepo;
import com.Contact.Management.System.Cms.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles contact CRUD, paginated listing, and first/last-name search,
 * always scoped to the owning user so one user can never read or modify
 * another user's contacts.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepo contactRepo;
    private final UserRepo userRepo;
    private final EmailRepo emailRepo;
    private final NumberRepo numberRepo;

    @Override
    public ContactResponse createContact(Long userId, ContactRequest request) {
        log.info("Creating contact for userId={}", userId);

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        validateRequest(request);

        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .title(request.getTitle())
                .user(user)
                .build();

        attachEmails(contact, request.getEmails());
        attachNumbers(contact, request.getNumbers());

        Contact saved = saveContact(contact);
        log.info("Contact created id={} for userId={}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Override
    public ContactResponse updateContact(Long userId, Long contactId, ContactRequest request) {
        log.info("Updating contactId={} for userId={}", contactId, userId);

        Contact contact = getOwnedContact(userId, contactId);
        validateRequest(request);

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        // orphanRemoval on Contact's collections means clearing here deletes the old rows
        contact.getEmails().clear();
        contact.getNumbers().clear();
        attachEmails(contact, request.getEmails());
        attachNumbers(contact, request.getNumbers());

        Contact saved = saveContact(contact);
        log.info("Contact updated id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    public void deleteContact(Long userId, Long contactId) {
        log.info("Deleting contactId={} for userId={}", contactId, userId);
        Contact contact = getOwnedContact(userId, contactId);
        contactRepo.delete(contact);
        log.info("Contact deleted id={}", contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContact(Long userId, Long contactId) {
        return toResponse(getOwnedContact(userId, contactId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getContacts(Long userId, Pageable pageable) {
        log.debug("Fetching paginated contacts for userId={} page={}", userId, pageable.getPageNumber());
        return contactRepo.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> searchContacts(Long userId, String keyword, Pageable pageable) {
        log.debug("Searching contacts for userId={} keyword='{}'", userId, keyword);
        if (!StringUtils.hasText(keyword)) {
            return getContacts(userId, pageable);
        }
        return contactRepo.searchByUserIdAndName(userId, keyword.trim(), pageable).map(this::toResponse);
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private Contact getOwnedContact(Long userId, Long contactId) {
        Contact contact = contactRepo.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));
        if (!contact.getUser().getId().equals(userId)) {
            log.warn("userId={} attempted to access contactId={} owned by another user", userId, contactId);
            // deliberately the same message as "not found" so we don't leak existence of other users' data
            throw new ResourceNotFoundException("Contact not found with id: " + contactId);
        }
        return contact;
    }

    private void validateRequest(ContactRequest request) {
        if (!StringUtils.hasText(request.getFirstName())) {
            throw new IllegalArgumentException("First name is required");
        }
        if (!StringUtils.hasText(request.getLastName())) {
            throw new IllegalArgumentException("Last name is required");
        }
    }

    /**
     * Saves the contact, translating a unique-constraint violation on the numbers
     * table (a race where two concurrent requests both pass the existsByNumber
     * check in {@link #attachNumbers}) into the existing 409 Conflict response
     * instead of letting it surface as a generic 500.
     */
    private Contact saveContact(Contact contact) {
        try {
            return contactRepo.save(contact);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Phone number already in use");
        }
    }

    private void attachEmails(Contact contact, List<EmailDto> emails) {
        if (emails == null) {
            return;
        }
        for (EmailDto dto : emails) {
            if (dto == null) {
                throw new IllegalArgumentException("Email entry must not be null");
            }
            if (!StringUtils.hasText(dto.getEmail())) {
                continue;
            }
            if (emailRepo.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + dto.getEmail());
            }
            EmailEntity entity = EmailEntity.builder()
                    .email(dto.getEmail())
                    .type(dto.getType())
                    .build();
            contact.addEmail(entity);
        }
    }

    private void attachNumbers(Contact contact, List<PhoneNumberDto> numbers) {
        if (numbers == null) {
            return;
        }
        for (PhoneNumberDto dto : numbers) {
            if (dto == null) {
                throw new IllegalArgumentException("Number entry must not be null");
            }
            if (!StringUtils.hasText(dto.getNumber())) {
                continue;
            }
            if (numberRepo.existsByNumber(dto.getNumber())) {
                throw new DuplicateResourceException("Phone number already in use: " + dto.getNumber());
            }
            PhoneNumberEntity entity = PhoneNumberEntity.builder()
                    .number(dto.getNumber())
                    .numberType(dto.getNumberType())
                    .build();
            contact.addNumber(entity);
        }
    }

    private ContactResponse toResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .title(contact.getTitle())
                .emails(contact.getEmails().stream()
                        .map(e -> EmailDto.builder()
                                .id(e.getId())
                                .email(e.getEmail())
                                .type(e.getType())
                                .build())
                        .collect(Collectors.toList()))
                .numbers(contact.getNumbers().stream()
                        .map(n -> PhoneNumberDto.builder()
                                .id(n.getId())
                                .number(n.getNumber())
                                .numberType(n.getNumberType())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
