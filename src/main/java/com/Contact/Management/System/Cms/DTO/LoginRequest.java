package com.Contact.Management.System.Cms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    private String loginIdentifier;
    private String password;
}
