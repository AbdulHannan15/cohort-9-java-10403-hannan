package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.Contact;
import com.Contact.Management.System.Cms.Entity.EmailEntity;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.SupportingEnum.EmailType;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("EmailRepo")
class EmailRepoTest {

    @Autowired
    private EmailRepo emailRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private UserRepo userRepo;

    @Test
    @DisplayName("existsByEmail reflects saved state")
    void existsByEmail() {
        User user = userRepo.save(User.builder()
                .loginIdentifier("owner@example.com").passwordHash("h").role(RoleEnum.USER).build());
        Contact contact = contactRepo.save(Contact.builder()
                .firstName("Jane").lastName("Doe").user(user).build());
        emailRepo.save(EmailEntity.builder()
                .email("jane@work.com").type(EmailType.WORK).contact(contact).build());

        assertThat(emailRepo.existsByEmail("jane@work.com")).isTrue();
        assertThat(emailRepo.existsByEmail("unused@example.com")).isFalse();
    }

    @Test
    @DisplayName("email uniqueness is enforced at the DB level")
    void email_isUnique() {
        User user = userRepo.save(User.builder()
                .loginIdentifier("owner@example.com").passwordHash("h").role(RoleEnum.USER).build());
        Contact contact = contactRepo.save(Contact.builder()
                .firstName("Jane").lastName("Doe").user(user).build());
        emailRepo.save(EmailEntity.builder()
                .email("jane@work.com").type(EmailType.WORK).contact(contact).build());

        EmailEntity duplicate = EmailEntity.builder()
                .email("jane@work.com").type(EmailType.PERSONAL).contact(contact).build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> emailRepo.saveAndFlush(duplicate)
        );
    }
}
