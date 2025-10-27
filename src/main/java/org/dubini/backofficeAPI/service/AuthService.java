package org.dubini.backofficeAPI.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.dubini.backofficeAPI.dto.request.LoginRequest;
import org.dubini.backofficeAPI.security.JwtProvider;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${app.backoffice.password}")
    private String backofficePasswordHash;

    public AuthService(PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public String login(LoginRequest request) {
        if (!passwordEncoder.matches(request.getPassword(), backofficePasswordHash)) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        return jwtProvider.generateToken();
    }

    public String refreshToken(String jwtToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }

}
