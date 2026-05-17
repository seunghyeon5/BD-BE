package com.example.demo.service;

import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 이메일 중복을 확인한 뒤 새 사용자를 저장
    public UserResponse signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }

        UserEntity user = UserEntity.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        return UserResponse.from(userRepository.save(user));
    }

    // 입력받은 이메일과 비밀번호가 저장된 사용자 정보와 일치하는지 확인
    public UserResponse login(UserLoginRequest request) {

        System.out.println("test");

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return UserResponse.from(user);
    }

    // 마이페이지 응답에 필요한 사용자 정보를 조회
    public UserResponse findMyPage(Long id) {
        return UserResponse.from(findUser(id));
    }

    // 다른 서비스에서도 재사용할 수 있는 사용자 조회 공통 메서드
    public UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));
    }

    // 사용자 탈퇴 메서드
    @Transactional
    public UserResponse withdrawUser(Long id) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));

        // 이미 탈퇴한 유저인지 확인
        if ("Y".equals(user.getDelYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 탈퇴한 사용자입니다.");
        }

        user.withdraw();

        return UserResponse.from(user);
    }
}
