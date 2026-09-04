package com.Contact.Management.System.Cms.Repo;

import com.Contact.Management.System.Cms.Entity.Contact;
import com.Contact.Management.System.Cms.Entity.PhoneNumberEntity;
import com.Contact.Management.System.Cms.Entity.User;
import com.Contact.Management.System.Cms.SupportingEnum.PhoneNumberType;
import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("NumberRepo")
class NumberRepoTest {

    @Autowired
    private NumberRepo numberRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private UserRepo userRepo;

    @Test
    @DisplayName("existsByNumber reflects saved state")
    void existsByNumber() {
        User user = userRepo.save(User.builder()
                .loginIdentifier("owner@example.com").passwordHash("h").role(RoleEnum.USER).build());
        Contact contact = contactRepo.save(Contact.builder()
                .firstName("Jane").lastName("Doe").user(user).build());
        numberRepo.save(PhoneNumberEntity.builder()
                .number("+15551234567").numberType(PhoneNumberType.OFFICE).contact(contact).build());

        assertThat(numberRepo.existsByNumber("+15551234567")).isTrue();
        assertThat(numberRepo.existsByNumber("+10000000000")).isFalse();
    }

    @Test
    @DisplayName("number uniqueness is enforced at the DB level")
    void number_isUnique() {
        User user = userRepo.save(User.builder()
                .loginIdentifier("owner@example.com").passwordHash("h").role(RoleEnum.USER).build());
        Contact contact = contactRepo.save(Contact.builder()
                .firstName("Jane").lastName("Doe").user(user).build());
        numberRepo.save(PhoneNumberEntity.builder()
                .number("+15551234567").numberType(PhoneNumberType.OFFICE).contact(contact).build());

        PhoneNumberEntity duplicate = PhoneNumberEntity.builder()
                .number("+15551234567").numberType(PhoneNumberType.PERSONAL).contact(contact).build();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> numberRepo.saveAndFlush(duplicate)
        );
    }
}
