package com.example.myFarm.user.service;

import com.example.myFarm.user.dto.MemberDTO;
import com.example.myFarm.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    /**
     * Spring Security가 로그인을 시도할 때 이 메서드를 호출합니다.
     * @param username (Spring Security의 'username'은 script.js의 'username',
     * 즉 DB의 'LOGIN_ID'를 의미합니다)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. [v12 수정] DB의 LOGIN_ID로 회원 정보를 찾습니다.
        MemberDTO member = memberMapper.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " 사용자를 찾을 수 없습니다."));

        // 2. Spring Security의 User 객체로 변환하여 반환합니다.
        return new User(
                member.getLoginId(),  // Principal (사용자 식별자, LOGIN_ID)
                member.getUserPw(),   // Credentials (자격 증명, 암호화된 PW)

                // Authorities (권한 목록)
                // USERS.AUTH 컬럼의 'ADMIN' 또는 'CONSUMER' 값을 사용
                Collections.singletonList(() -> "ROLE_" + member.getAuth())
        );
    }
}