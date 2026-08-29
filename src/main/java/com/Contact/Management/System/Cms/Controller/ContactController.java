package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.ContactRequest;
import com.Contact.Management.System.Cms.DTO.ContactResponse;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.createContact(principal.getUserId(), request));
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<ContactResponse> update(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long contactId,
                                                    @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.updateContact(principal.getUserId(), contactId, request));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                        @PathVariable Long contactId) {
        contactService.deleteContact(principal.getUserId(), contactId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<ContactResponse> get(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long contactId) {
        return ResponseEntity.ok(contactService.getContact(principal.getUserId(), contactId));
    }

    /** GET /api/contacts?search=jane&page=0&size=20&sort=lastName,asc */
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> list(@AuthenticationPrincipal CustomUserDetails principal,
                                                        @RequestParam(required = false) String search,
                                                        Pageable pageable) {
        Page<ContactResponse> page = (search == null || search.isBlank())
                ? contactService.getContacts(principal.getUserId(), pageable)
                : contactService.searchContacts(principal.getUserId(), search, pageable);
        return ResponseEntity.ok(page);
    }
}
