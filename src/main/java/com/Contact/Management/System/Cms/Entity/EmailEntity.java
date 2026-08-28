package com.Contact.Management.System.Cms.Entity;

import com.Contact.Management.System.Cms.SupportingEnum.EmailType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "contact")
@Table(
        name = "emails",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

}
