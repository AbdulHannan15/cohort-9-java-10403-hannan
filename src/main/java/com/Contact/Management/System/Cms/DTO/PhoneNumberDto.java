package com.Contact.Management.System.Cms.DTO;

import com.Contact.Management.System.Cms.SupportingEnum.PhoneNumberType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneNumberDto {

    private Long id;
    private String number;
    private PhoneNumberType numberType;
}
