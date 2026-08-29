package com.Contact.Management.System.Cms.DTO;

import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType; // "Bearer"
    private Long userId;
    private String loginIdentifier;
    private RoleEnum role;
}
