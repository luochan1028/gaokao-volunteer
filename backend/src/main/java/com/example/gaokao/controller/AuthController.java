
package com.example.gaokao.controller;

import com.example.gaokao.dto.request.ChangePasswordRequest;
import com.example.gaokao.dto.request.LoginRequest;
import com.example.gaokao.dto.request.RegisterRequest;
import com.example.gaokao.dto.request.UserInfoRequest;
import com.example.gaokao.dto.response.ApiResponse;
import com.example.gaokao.dto.response.LoginResponse;
import com.example.gaokao.entity.User;
import com.example.gaokao.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: {}", request.getPhone());
        LoginResponse response = userService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册: {}", request.getPhone());
        userService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);
        return ApiResponse.success(user);
    }

    @PutMapping("/me")
    public ApiResponse<User> updateUserInfo(@Valid @RequestBody UserInfoRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);
        User updatedUser = userService.updateUserInfo(user.getId(), request);
        return ApiResponse.success("更新成功", updatedUser);
    }

    @GetMapping("/check-phone/{phone}")
    public ApiResponse<Boolean> checkPhoneExists(@PathVariable String phone) {
        boolean exists = userService.isPhoneExists(phone);
        return ApiResponse.success(exists);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phone = authentication.getName();
        User user = userService.getUserByPhone(phone);
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码修改成功", null);
    }
}
