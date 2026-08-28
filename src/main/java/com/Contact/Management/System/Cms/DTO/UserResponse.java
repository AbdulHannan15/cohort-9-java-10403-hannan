package com.Contact.Management.System.Cms.DTO;

import com.Contact.Management.System.Cms.SupportingEnum.RoleEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String loginIdentifier;
    private String recoveryPhone;
    private RoleEnum role;
}
