package com.Contact.Management.System.Cms.DTO;

import com.Contact.Management.System.Cms.SupportingEnum.NameTitle;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private NameTitle title;
    private List<EmailDto> emails;
    private List<PhoneNumberDto> numbers;
}
