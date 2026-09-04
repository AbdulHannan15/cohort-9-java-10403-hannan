package com.Contact.Management.System.Cms.Controller;

import com.Contact.Management.System.Cms.DTO.ContactResponse;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.Security.CustomUserDetails;
import com.Contact.Management.System.Cms.Service.ContactService;
import com.Contact.Management.System.Cms.SupportingEnum.NameTitle;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ContactController")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    private CustomUserDetails principal() {
        User user = User.builder().id(1L).loginIdentifier("jane@example.com")
                .passwordHash("hash").role(RoleEnum.USER).build();
        return new CustomUserDetails(user);
    }

    private static final String SAMPLE_REQUEST_BODY = """
            {"firstName":"Jane","lastName":"Doe","title":"MS","emails":[],"numbers":[]}
            """;

    private ContactResponse sampleResponse() {
        return ContactResponse.builder()
                .id(100L).firstName("Jane").lastName("Doe").title(NameTitle.MS)
                .emails(List.of()).numbers(List.of())
                .build();
    }

    @Test
    @DisplayName("POST /api/contacts creates a contact for the authenticated user")
    void create_returnsCreatedContact() throws Exception {
        when(contactService.createContact(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/contacts")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLE_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @DisplayName("PUT /api/contacts/{id} updates a contact")
    void update_returnsUpdatedContact() throws Exception {
        when(contactService.updateContact(eq(1L), eq(100L), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/contacts/100")
                        .with(user(principal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAMPLE_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("DELETE /api/contacts/{id} deletes a contact and returns 204")
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/contacts/100").with(user(principal())))
                .andExpect(status().isNoContent());

        verify(contactService).deleteContact(1L, 100L);
    }

    @Test
    @DisplayName("GET /api/contacts/{id} returns a single contact")
    void get_returnsContact() throws Exception {
        when(contactService.getContact(1L, 100L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/contacts/100").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("GET /api/contacts without a search param returns the paginated listing")
    void list_withoutSearch_usesPlainListing() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ContactResponse> page = new PageImpl<>(List.of(sampleResponse()), pageable, 1);
        when(contactService.getContacts(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(contactService).getContacts(eq(1L), any());
    }

    @Test
    @DisplayName("GET /api/contacts?search= uses the search endpoint")
    void list_withSearch_usesSearch() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ContactResponse> page = new PageImpl<>(List.of(sampleResponse()), pageable, 1);
        when(contactService.searchContacts(eq(1L), eq("jane"), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts").param("search", "jane").with(user(principal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Jane"));

        verify(contactService).searchContacts(eq(1L), eq("jane"), any());
    }
}
