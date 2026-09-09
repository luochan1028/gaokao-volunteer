
package com.example.gaokao.security;

import com.example.gaokao.entity.User;
import com.example.gaokao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findActiveUserByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),
                user.getPassword(),
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
