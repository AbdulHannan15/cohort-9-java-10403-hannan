package com.Contact.Management.System.Cms;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="numbers",
        uniqueConstraints = @UniqueConstraint(columnNames = "number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneNumberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhoneNumberEntity numberType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Contact contact;


}



