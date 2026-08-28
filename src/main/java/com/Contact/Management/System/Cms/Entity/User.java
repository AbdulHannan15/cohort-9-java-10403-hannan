package com.Contact.Management.System.Cms.Entity;

import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // to recover if user forget password
    @Column(nullable = false, unique = true)
    private String loginIdentifier;   // email to register/login


    @Setter(AccessLevel.NONE)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(unique = true)
    private String recoveryPhone;     // to recover if user forget password

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setUser(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setUser(null);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoleEnum role = RoleEnum.USER;

    /*
      The only way to change the hash directly on an existing entity.
      Callers must already have run the raw password through
      PasswordEncoder.encode() — this method just guards against blanks.
     */
    public void setPasswordHash(String encodedHash) {
        if (encodedHash == null || encodedHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be blank");
        }
        this.passwordHash = encodedHash;
    }

    /*
      Lombok let you declare in .builder so i have to do to prevent builder
      from skipping password encryption.
     */
    public static class UserBuilder {
        public UserBuilder passwordHash(String encodedHash) {
            if (encodedHash == null || encodedHash.isBlank()) {
                throw new IllegalArgumentException("Password hash cannot be blank");
            }
            this.passwordHash = encodedHash;
            return this;
        }
    }
}
