package com.siso.user.infrastructure.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;

public class TokenAuthentication extends PreAuthenticatedAuthenticationToken {

    public TokenAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public TokenAuthentication(Object principal, Object credentials,
                               Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}