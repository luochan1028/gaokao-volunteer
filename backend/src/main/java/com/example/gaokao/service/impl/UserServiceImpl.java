
package com.example.gaokao.service.impl;

import com.example.gaokao.dto.request.LoginRequest;
import com.example.gaokao.dto.request.RegisterRequest;
import com.example.gaokao.dto.request.UserInfoRequest;
import com.example.gaokao.dto.response.LoginResponse;
import com.example.gaokao.entity.User;
import com.example.gaokao.repository.UserRepository;
import com.example.gaokao.service.UserService;
import com.example.gaokao.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByPhone(request.getPhone()).orElseThrow();

        String token = jwtUtil.generateToken(user.getPhone(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationInSeconds())
                .user(LoginResponse.UserDTO.builder()
                        .id(user.getId())
                        .phone(user.getPhone())
                        .name(user.getName())
                        .role(user.getRole().name())
                        .userType(user.getUserType() != null ? user.getUserType().name() : null)
                        .province(user.getProvince())
                        .hasCompletedInfo(hasCompletedInfo(user))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.ROLE_USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("用户注册成功: {}", request.getPhone());
    }

    @Override
    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public User updateUserInfo(Long userId, UserInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setName(request.getName());
        user.setProvince(request.getProvince());
        user.setExamYear(request.getExamYear());
        user.setSelectedSubjects(request.getSelectedSubjects());
        user.setSubjectDetails(request.getSubjectDetails());
        user.setTotalScore(request.getTotalScore());
        user.setRank(request.getRank());
        user.setScienceOrArts(request.getScienceOrArts());
        user.setIdCard(request.getIdCard());

        if (request.getUserType() != null) {
            try {
                user.setUserType(User.UserType.valueOf(request.getUserType()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的用户类型: {}", request.getUserType());
            }
        }

        return userRepository.save(user);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    private boolean hasCompletedInfo(User user) {
        return user.getName() != null &&
                user.getProvince() != null &&
                user.getSelectedSubjects() != null &&
                !user.getSelectedSubjects().isEmpty();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户密码修改成功: {}", user.getPhone());
    }

    @lombok.Builder(builderMethodName = "builder")
    private User() {}
}
