package com.example.myFarm.user.mapper;

import com.example.myFarm.user.dto.AdressDTO;
import com.example.myFarm.user.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface MemberMapper {

    /**
     * [v12] USERS 테이블의 LOGIN_ID로 회원 찾기 (Security용)
     */
    Optional<MemberDTO> findByLoginId(String loginId);

    /**
     * [v12] USERS 테이블에 회원 정보 삽입
     */
    void insertUser(MemberDTO member);

    /**
     * [v12] ADDRESS 테이블에 주소 정보 삽입
     */
    void insertAddress(AdressDTO address);
}