package com.Contact.Management.System.Cms.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.Contact.Management.System.Cms.SupportingEnum.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "owner")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private NameTitle title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmailEntity> emails = new ArrayList<>();

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PhoneNumberEntity> numbers = new ArrayList<>();

    public void addEmail(EmailEntity email) {
        emails.add(email);
        email.setContact(this);
    }

    public void removeEmail(EmailEntity email) {
        emails.remove(email);
        email.setContact(null);
    }

    public void addNumber(PhoneNumberEntity number) {
        numbers.add(number);
        number.setContact(this);
    }

    public void removeNumber(PhoneNumberEntity number) {
        numbers.remove(number);
        number.setContact(null);
    }
}
