package com.Contact.Management.System.Cms.DTO;

import com.Contact.Management.System.Cms.SupportingEnum.EmailType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDto {

    private Long id;
    private String email;
    private EmailType type;
}
