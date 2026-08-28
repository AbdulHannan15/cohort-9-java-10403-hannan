package com.Contact.Management.System.Cms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String loginIdentifier; // email or phone number, per self-registration requirement
    private String password;
    private String recoveryPhone;   // optional, used for password recovery
}
