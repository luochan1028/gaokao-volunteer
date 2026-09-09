
package com.example.gaokao.service;

import com.example.gaokao.dto.request.LoginRequest;
import com.example.gaokao.dto.request.RegisterRequest;
import com.example.gaokao.dto.request.UserInfoRequest;
import com.example.gaokao.dto.response.LoginResponse;
import com.example.gaokao.entity.User;

public interface UserService {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    User getUserByPhone(String phone);

    User getUserById(Long id);

    User updateUserInfo(Long userId, UserInfoRequest request);

    boolean isPhoneExists(String phone);

    void changePassword(Long userId, String oldPassword, String newPassword);

    String sendVerificationCode(String phone);
}
