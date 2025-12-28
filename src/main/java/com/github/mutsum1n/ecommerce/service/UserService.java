package com.github.mutsum1n.ecommerce.service;

import com.github.mutsum1n.ecommerce.dto.RegisterRequest;
import com.github.mutsum1n.ecommerce.entity.User;
import com.github.mutsum1n.ecommerce.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));

        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_BUYER";
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if ("SELLER".equalsIgnoreCase(role) || "ROLE_SELLER".equalsIgnoreCase(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_BUYER"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + username));
    }

    public List<User> getAllBuyers() {
        return userRepository.findByRole("BUYER");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + id));
    }

    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = getUserById(userId);
        user.setRole(role);
        userRepository.save(user);
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
            if (userRepository.existsByPhone(registerRequest.getPhone())) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole());

        return userRepository.save(user);
    }

}