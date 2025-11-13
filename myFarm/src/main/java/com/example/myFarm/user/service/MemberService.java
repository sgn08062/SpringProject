package com.example.myFarm.user.service;

import com.example.myFarm.user.dto.AdressDTO;
import com.example.myFarm.user.dto.MemberDTO;
import com.example.myFarm.user.dto.SignupRequestDTO;
import com.example.myFarm.user.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 주입

    /**
     * [v12] 회원가입을 처리하는 서비스
     * (USERS 테이블과 ADDRESS 테이블에 나눠서 저장)
     *
     * @param request (script.js 폼에서 받은 SignupRequestDTO)
     */
    @Transactional // 두 개의 쿼리가 모두 성공해야 하므로 트랜잭션 처리
    public void register(SignupRequestDTO request) {

        // 1. USERS 테이블에 저장할 MemberDTO 생성
        MemberDTO member = new MemberDTO();
        member.setLoginId(request.getUsername());
        member.setUserPw(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        member.setUserName(request.getName());
        member.setPhone(request.getPhone());
        // auth는 DB DEFAULT ('CONSUMER') 사용
        // email은 script.js에서 받지 않음

        // 1-1. USERS 테이블에 삽입
        memberMapper.insertUser(member);
        // (이때 MemberDTO의 userId 필드에 auto-increment된 USER_ID가 채워짐)

        // 2. ADDRESS 테이블에 저장할 AddressDTO 생성
        // 주소가 비어있지 않은 경우에만 저장
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            AdressDTO address = new AdressDTO();
            address.setAddress(request.getAddress());
            address.setAddName("기본 배송지"); // 배송지 이름은 기본값으로 설정

            // 1-1에서 생성된 USER_ID를 FK로 사용
            address.setUserId(member.getUserId());

            // 2-1. ADDRESS 테이블에 삽입
            memberMapper.insertAddress(address);
        }
    }
}