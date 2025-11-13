package com.example.myFarm.user.controller;

import com.example.myFarm.user.dto.LoginRequestDTO;
import com.example.myFarm.user.dto.MemberDTO;
import com.example.myFarm.user.dto.SignupRequestDTO; // [v12] 수정
import com.example.myFarm.user.mapper.MemberMapper;
import com.example.myFarm.user.service.MemberService; // [v12] 수정
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final AuthenticationManager authenticationManager;
    private final MemberMapper memberMapper; // /me, /check-id에서 사용
    private final MemberService memberService; // /signup에서 사용

    /**
     * [GET /api/member/check-id]
     * (script.js의 'username'은 DB의 'loginId'임)
     */
    @GetMapping("/check-id")
    public ResponseEntity<String> checkId(@RequestParam("username") String username) {
        if (memberMapper.findByLoginId(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        }
    }

    /**
     * [POST /api/member/signup]
     * [v12] 복잡한 로직을 MemberService로 위임
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDTO request) {
        try {
            // Service가 USERS, ADDRESS 테이블에 나눠서 저장
            memberService.register(request);
            return ResponseEntity.ok("회원가입에 성공했습니다.");

        } catch (Exception e) {
            // DB의 UNIQUE 제약조건(loginId) 위반 시
            if (e.getMessage().contains("UNIQUE") || e.getMessage().contains("Duplicate")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
            }
            e.printStackTrace(); // 서버 로그에 에러 출력
            return ResponseEntity.badRequest().body("회원가입 중 오류 발생 (관리자 문의)");
        }
    }

    /**
     * [POST /api/member/login]
     * (script.js의 'username'은 DB의 'loginId'임)
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), // (이것이 loginId)
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return ResponseEntity.ok("로그인 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * [POST /api/member/logout]
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    /**
     * [GET /api/member/me]
     * (UserDetails.getUsername()은 DB의 'loginId'를 반환)
     */
    @GetMapping("/me")
    public ResponseEntity<MemberDTO> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MemberDTO member = memberMapper.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        member.setUserPw(null); // 비밀번호 제거
        return ResponseEntity.ok(member);
    }
}