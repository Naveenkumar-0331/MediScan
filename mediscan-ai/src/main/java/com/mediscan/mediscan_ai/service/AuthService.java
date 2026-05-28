package com.mediscan.mediscan_ai.service;

import com.mediscan.mediscan_ai.dto.request.LoginRequest;
import com.mediscan.mediscan_ai.dto.request.RegisterRequest;
import com.mediscan.mediscan_ai.dto.response.AuthResponse;
import com.mediscan.mediscan_ai.entity.mysql.Role;
import com.mediscan.mediscan_ai.entity.mysql.User;
import com.mediscan.mediscan_ai.repository.mysql.RoleRepository;
import com.mediscan.mediscan_ai.repository.mysql.UserRepository;
import com.mediscan.mediscan_ai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request)
    {

        if(userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already registered");

        Role role=roleRepository.findByName(Role.ERole.valueOf("ROLE_"+request.getRole()))
                .orElseThrow(()-> new RuntimeException("Role not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();
        userRepository.save(user);


        return AuthResponse.builder()
                .message("Registration successful")
                .email(user.getEmail())
                .role(request.getRole())
                .build();
    }
    public AuthResponse login(LoginRequest request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getEmail());
        String roleName = user.getRoles().iterator().next().getName().name();

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(roleName)
                .message("Login successful")
                .build();
    }

}
