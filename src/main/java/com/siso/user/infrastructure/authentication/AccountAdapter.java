package com.siso.user.infrastructure.authentication;

import com.siso.user.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


@Getter
public class AccountAdapter implements UserDetails, OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;

    public AccountAdapter(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // 소셜 로그인 아닌 경우 사용할 생성자
    public AccountAdapter(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
    }

    // UserDetails 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 "USER" 권한만 부여
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // 소셜 로그인은 비밀번호를 사용하지 않으므로 null 반환
        return null;
    }

    @Override
    public String getUsername() {
        // 사용자 식별자를 반환
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isBlock();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return !user.isDeleted();
    }

    // OAuth2User 구현
    @Override
    public String getName() {
        return user.getId().toString(); // JWT sub용, DB PK 사용
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}