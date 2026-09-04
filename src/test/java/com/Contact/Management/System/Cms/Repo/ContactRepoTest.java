package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.Contact;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.SupportingEnum.NameTitle;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ContactRepo")
class ContactRepoTest {

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private UserRepo userRepo;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = userRepo.save(User.builder()
                .loginIdentifier("owner@example.com").passwordHash("h").role(RoleEnum.USER).build());
        otherUser = userRepo.save(User.builder()
                .loginIdentifier("other@example.com").passwordHash("h").role(RoleEnum.USER).build());

        contactRepo.save(Contact.builder().firstName("Jane").lastName("Doe").title(NameTitle.MS).user(owner).build());
        contactRepo.save(Contact.builder().firstName("John").lastName("Smith").title(NameTitle.MR).user(owner).build());
        contactRepo.save(Contact.builder().firstName("Janet").lastName("Wong").title(NameTitle.DR).user(owner).build());
        // belongs to a different user - must never show up in owner's results
        contactRepo.save(Contact.builder().firstName("Jane").lastName("Rival").title(NameTitle.MS).user(otherUser).build());
    }

    @Test
    @DisplayName("findByUserId only returns contacts owned by that user")
    void findByUserId_scopedToOwner() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Contact> result = contactRepo.findByUserId(owner.getId(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting(Contact::getLastName)
                .containsExactlyInAnyOrder("Doe", "Smith", "Wong");
    }

    @Test
    @DisplayName("findByUserId respects pagination")
    void findByUserId_paginates() {
        Pageable firstPage = PageRequest.of(0, 2);

        Page<Contact> result = contactRepo.findByUserId(owner.getId(), firstPage);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("searchByUserIdAndName matches first or last name, case-insensitively, scoped to the owner")
    void searchByUserIdAndName_matchesFirstOrLastName() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Contact> byFirstName = contactRepo.searchByUserIdAndName(owner.getId(), "jan", pageable);
        assertThat(byFirstName.getContent()).extracting(Contact::getFirstName)
                .containsExactlyInAnyOrder("Jane", "Janet");

        Page<Contact> bySurname = contactRepo.searchByUserIdAndName(owner.getId(), "smith", pageable);
        assertThat(bySurname.getContent()).hasSize(1);
        assertThat(bySurname.getContent().get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("searchByUserIdAndName never returns another user's contacts")
    void searchByUserIdAndName_scopedToOwner() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Contact> result = contactRepo.searchByUserIdAndName(owner.getId(), "jane", pageable);

        assertThat(result.getContent()).extracting(Contact::getLastName)
                .containsExactly("Doe"); // not "Rival", which belongs to otherUser
    }
}
