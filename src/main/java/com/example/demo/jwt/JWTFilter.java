package com.example.demo.jwt;

import com.example.demo.entity.UserEntity;
import com.example.demo.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.ToString;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.demo.repository.UserRepository;

@ToString
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    // 요청의 Authorization 헤더에 담긴 JWT를 검증하고 인증 정보를 SecurityContext에 저장한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 이후의 토큰 값만 추출
        String token = authorizationHeader.substring(7);

        try {
            if (jwtUtil.isTokenExpired(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 토큰이 만료되었습니다.");
                return;
            }

            String email = jwtUtil.getEmail(token);
//            String role = jwtUtil.getRole(token);
//            UserDetails userDetails = User.builder()
//                    .username(email)
//                    .password("")
//                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
//                    .build();
//
//            Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 토큰의 이메일로 DB에서 실제 회원 엔티티 조회
            UserEntity userEntity = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다: " + email));

            // CustomUserDetails에 엔티티를 담아서 생성
            CustomUserDetails userDetails = new CustomUserDetails(userEntity);
            System.out.println("====== [유저 인증 정보 확인] ======");
            System.out.println("ID: " + userDetails.getUserId());
            System.out.println("이메일: " + userDetails.getUsername());
            System.out.println("권한: " + userDetails.getAuthorities());
            System.out.println("==================================");
            System.out.println("정보 보기: " + userDetails);

            // Principal 자리에 문자열이나 기본 User가 아닌 'userDetails' 객체를 통째로 주입
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            log.error("JWT 필터 처리 중 오류 발생: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        filterChain.doFilter(request, response);
    }
}
