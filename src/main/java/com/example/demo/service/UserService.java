package com.example.demo.service;

import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserDuplicateCheckResponse;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.jwt.JWTUtil;
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
    private final JWTUtil jWTUtil;

    // 이메일 중복을 확인한 뒤 새 사용자를 저장
    public UserResponse signup(UserSignupRequest request) {
        validateEmailAvailable(request.getEmail());
        validateNicknameAvailable(request.getNickname());

        UserEntity user = UserEntity.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getGender(),
                request.getPhoneNum()
        );

        return UserResponse.from(userRepository.save(user));
    }

    // 이메일 중복 확인
    public UserDuplicateCheckResponse checkEmailDuplicate(String email) {
        return UserDuplicateCheckResponse.builder()
                .field("email")
                .value(email)
                .available(!userRepository.existsByEmail(email))
                .build();
    }

    // 닉네임 중복 확인
    public UserDuplicateCheckResponse checkNicknameDuplicate(String nickname) {
        return UserDuplicateCheckResponse.builder()
                .field("nickname")
                .value(nickname)
                .available(!userRepository.existsByNickname(nickname))
                .build();
    }

    // 입력받은 이메일과 비밀번호가 저장된 사용자 정보와 일치하는지 확인
    public LoginResponse login(UserLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String role = "ROLE_USER";
        String token = jWTUtil.createJwt(
                user.getEmail(),
                role,
                60 * 60 * 1000L
        );

        return LoginResponse.of(token, user);
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

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 가입된 이메일입니다.");
        }
    }

    private void validateNicknameAvailable(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
    }
}
