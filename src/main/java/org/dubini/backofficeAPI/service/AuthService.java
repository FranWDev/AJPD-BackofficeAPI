package org.dubini.backofficeAPI.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.dubini.backofficeAPI.dto.request.LoginRequest;
import org.dubini.backofficeAPI.security.JwtProvider;
import org.dubini.backofficeAPI.config.AccessKeyProperties;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AccessKeyProperties accessKeyProperties;

    public AuthService(PasswordEncoder passwordEncoder, JwtProvider jwtProvider,
            AccessKeyProperties accessKeyProperties) {
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.accessKeyProperties = accessKeyProperties;
    }

    public String login(LoginRequest request) {
        if (!passwordEncoder.matches(request.getPassword(), accessKeyProperties.getKeyHash())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        return jwtProvider.generateToken();
    }

    public String refreshToken(String jwtToken) {
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }
}
