package com.Contact.Management.System.Cms.Entity;


import com.Contact.Management.System.Cms.SupportingEnum.PhoneNumberType;
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
    private PhoneNumberType numberType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Contact contact;


}



