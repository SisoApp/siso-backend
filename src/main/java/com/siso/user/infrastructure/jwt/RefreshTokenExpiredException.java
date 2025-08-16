package com.siso.user.infrastructure.jwt;

import org.springframework.security.core.AuthenticationException;

public class RefreshTokenExpiredException extends AuthenticationException {
    public RefreshTokenExpiredException(String msg) {
        super(msg);

    }
}
