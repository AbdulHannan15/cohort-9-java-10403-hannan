package com.Contact.Management.System.Cms.Service;

import com.Contact.Management.System.Cms.DTO.ContactRequest;
import com.Contact.Management.System.Cms.DTO.ContactResponse;
import com.Contact.Management.System.Cms.DTO.EmailDto;
import com.Contact.Management.System.Cms.DTO.PhoneNumberDto;
import com.Contact.Management.System.Cms.Entity.Contact;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Exception.DuplicateResourceException;
import com.Contact.Management.System.Cms.Exception.ResourceNotFoundException;
import com.Contact.Management.System.Cms.Repo.ContactRepo;
import com.Contact.Management.System.Cms.Repo.EmailRepo;
import com.Contact.Management.System.Cms.Repo.NumberRepo;
import com.Contact.Management.System.Cms.Repo.UserRepo;
import com.Contact.Management.System.Cms.SupportingEnum.EmailType;
import com.Contact.Management.System.Cms.SupportingEnum.NameTitle;
import com.Contact.Management.System.Cms.SupportingEnum.PhoneNumberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactServiceImpl")
class ContactServiceImplTest {

    @Mock private ContactRepo contactRepo;
    @Mock private UserRepo userRepo;
    @Mock private EmailRepo emailRepo;
    @Mock private NumberRepo numberRepo;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User owner;
    private Contact ownedContact;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).loginIdentifier("owner@example.com").passwordHash("h").build();

        ownedContact = Contact.builder()
                .id(100L)
                .firstName("Jane")
                .lastName("Doe")
                .title(NameTitle.MS)
                .user(owner)
                .build();
    }

    private ContactRequest sampleRequest() {
        return ContactRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .title(NameTitle.MS)
                .emails(List.of(EmailDto.builder().email("jane@work.com").type(EmailType.WORK).build()))
                .numbers(List.of(PhoneNumberDto.builder().number("+15551234567").numberType(PhoneNumberType.OFFICE).build()))
                .build();
    }

    @Nested
    @DisplayName("createContact")
    class CreateContact {

        @Test
        @DisplayName("saves a contact with emails and numbers attached")
        void createContact_success() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
            when(emailRepo.existsByEmail("jane@work.com")).thenReturn(false);
            when(numberRepo.existsByNumber("+15551234567")).thenReturn(false);
            when(contactRepo.save(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(100L);
                return c;
            });

            ContactResponse response = contactService.createContact(1L, sampleRequest());

            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getFirstName()).isEqualTo("Jane");
            assertThat(response.getEmails()).hasSize(1);
            assertThat(response.getNumbers()).hasSize(1);
        }

        @Test
        @DisplayName("throws when the owning user does not exist")
        void createContact_userNotFound_throws() {
            when(userRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.createContact(1L, sampleRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(contactRepo);
        }

        @Test
        @DisplayName("rejects a blank first name")
        void createContact_blankFirstName_throws() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
            ContactRequest request = sampleRequest();
            request.setFirstName("  ");

            assertThatThrownBy(() -> contactService.createContact(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a blank last name")
        void createContact_blankLastName_throws() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
            ContactRequest request = sampleRequest();
            request.setLastName("");

            assertThatThrownBy(() -> contactService.createContact(1L, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a duplicate email address")
        void createContact_duplicateEmail_throws() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
            when(emailRepo.existsByEmail("jane@work.com")).thenReturn(true);

            assertThatThrownBy(() -> contactService.createContact(1L, sampleRequest()))
                    .isInstanceOf(DuplicateResourceException.class);
            verify(contactRepo, never()).save(any());
        }

        @Test
        @DisplayName("rejects a duplicate phone number")
        void createContact_duplicateNumber_throws() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(owner));
            when(emailRepo.existsByEmail("jane@work.com")).thenReturn(false);
            when(numberRepo.existsByNumber("+15551234567")).thenReturn(true);

            assertThatThrownBy(() -> contactService.createContact(1L, sampleRequest()))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("updateContact / deleteContact / getContact (ownership)")
    class Ownership {

        @Test
        @DisplayName("updateContact succeeds for the owning user")
        void updateContact_success() {
            when(contactRepo.findById(100L)).thenReturn(Optional.of(ownedContact));
            when(emailRepo.existsByEmail(anyString())).thenReturn(false);
            when(numberRepo.existsByNumber(anyString())).thenReturn(false);
            when(contactRepo.save(any(Contact.class))).thenReturn(ownedContact);

            ContactRequest request = sampleRequest();
            request.setFirstName("Janet");

            ContactResponse response = contactService.updateContact(1L, 100L, request);

            assertThat(response.getFirstName()).isEqualTo("Janet");
        }

        @Test
        @DisplayName("updateContact throws not-found for a non-owning user")
        void updateContact_wrongOwner_throws() {
            when(contactRepo.findById(100L)).thenReturn(Optional.of(ownedContact));

            assertThatThrownBy(() -> contactService.updateContact(2L, 100L, sampleRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(contactRepo, never()).save(any());
        }

        @Test
        @DisplayName("updateContact throws when the contact does not exist")
        void updateContact_notFound_throws() {
            when(contactRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.updateContact(1L, 999L, sampleRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("deleteContact succeeds for the owning user")
        void deleteContact_success() {
            when(contactRepo.findById(100L)).thenReturn(Optional.of(ownedContact));

            contactService.deleteContact(1L, 100L);

            verify(contactRepo).delete(ownedContact);
        }

        @Test
        @DisplayName("deleteContact throws not-found for a non-owning user")
        void deleteContact_wrongOwner_throws() {
            when(contactRepo.findById(100L)).thenReturn(Optional.of(ownedContact));

            assertThatThrownBy(() -> contactService.deleteContact(2L, 100L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(contactRepo, never()).delete(any(Contact.class));
        }

        @Test
        @DisplayName("getContact throws not-found for a non-owning user")
        void getContact_wrongOwner_throws() {
            when(contactRepo.findById(100L)).thenReturn(Optional.of(ownedContact));

            assertThatThrownBy(() -> contactService.getContact(2L, 100L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getContacts / searchContacts")
    class Listing {

        @Test
        @DisplayName("getContacts returns a mapped page scoped to the user")
        void getContacts_paginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(ownedContact), pageable, 1);
            when(contactRepo.findByUserId(1L, pageable)).thenReturn(page);

            Page<ContactResponse> result = contactService.getContacts(1L, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("searchContacts delegates to the name-search query when a keyword is given")
        void searchContacts_withKeyword() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(ownedContact), pageable, 1);
            when(contactRepo.searchByUserIdAndName(eq(1L), eq("jane"), eq(pageable))).thenReturn(page);

            Page<ContactResponse> result = contactService.searchContacts(1L, "jane", pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(contactRepo, never()).findByUserId(anyLong(), any());
        }

        @Test
        @DisplayName("searchContacts falls back to the plain listing when the keyword is blank")
        void searchContacts_blankKeyword_fallsBackToListing() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(ownedContact), pageable, 1);
            when(contactRepo.findByUserId(1L, pageable)).thenReturn(page);

            contactService.searchContacts(1L, "   ", pageable);

            verify(contactRepo).findByUserId(1L, pageable);
            verify(contactRepo, never()).searchByUserIdAndName(anyLong(), anyString(), any());
        }
    }
}
