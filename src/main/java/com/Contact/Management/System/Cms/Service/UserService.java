package com.Contact.Management.System.Cms.Service;

import com.Contact.Management.System.Cms.DTO.ChangePasswordRequest;
import com.Contact.Management.System.Cms.DTO.LoginRequest;
import com.Contact.Management.System.Cms.DTO.RegisterRequest;
import com.Contact.Management.System.Cms.DTO.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse getUserById(Long userId);
}
