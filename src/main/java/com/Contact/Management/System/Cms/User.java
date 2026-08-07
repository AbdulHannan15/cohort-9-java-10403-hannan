package com.Contact.Management.System.Cms;

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
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // to recover if user forget password
    @Column(nullable = false, unique = true)
    private String loginIdentifier;   // email to register/login

    @Column(nullable = false)
    private String password;          // Will be make safe at service level



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


}