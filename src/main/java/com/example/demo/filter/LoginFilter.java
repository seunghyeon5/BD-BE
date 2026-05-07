package com.example.demo.filter;

import com.example.demo.dto.UserLoginRequest;
import com.example.demo.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final long ACCESS_TOKEN_EXPIRED_MS = 60 * 60 * 1000L;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    // 로그인 필터가 사용할 AuthenticationManager와 JWT 생성 유틸을 주입하고 처리 URL을 지정한다.
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/users/login");
    }

    // 로그인 요청의 JSON body에서 이메일과 비밀번호를 읽어 Spring Security 인증을 시도한다.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            UserLoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), UserLoginRequest.class);
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청 형식이 올바르지 않습니다.", e);
        }
    }

    // 인증 성공 시 JWT를 발급하고 Authorization 헤더와 응답 body에 함께 담아 반환한다.
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) throws IOException {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority();
        String token = jwtUtil.createJwt(email, role, ACCESS_TOKEN_EXPIRED_MS);

        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.addHeader("Authorization", "Bearer " + token);
        objectMapper.writeValue(res.getWriter(), Map.of("token", token, "tokenType", "Bearer"));
    }

    // 인증 실패 시 클라이언트에 401 상태와 실패 메시지를 반환한다.
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res, AuthenticationException failed) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다."));
    }
}
