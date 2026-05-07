package com.example.demo.security;

import com.example.demo.entity.UserEntity;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private final UserEntity user;

    // 현재 사용자에게 부여된 권한 목록을 반환한다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(DEFAULT_ROLE));
    }

    // DB에 저장된 암호화 비밀번호를 Spring Security 인증에 사용하도록 반환한다.
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Spring Security의 username으로 사용할 이메일을 반환한다.
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 만료 정책을 사용하지 않으므로 항상 true를 반환한다.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 정책을 사용하지 않으므로 항상 true를 반환한다.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 정책을 사용하지 않으므로 항상 true를 반환한다.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 정책을 사용하지 않으므로 항상 true를 반환한다.
    @Override
    public boolean isEnabled() {
        return true;
    }
}
