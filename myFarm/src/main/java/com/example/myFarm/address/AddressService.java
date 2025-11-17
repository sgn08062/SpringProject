package com.example.myFarm.address;


import com.example.myFarm.command.AddressVO;
import java.util.List;

public interface AddressService {

    // 새로운 주소 등록
    int addressInsert(AddressVO addressVO);

    // 기존 주소 수정
    int addressUpdate(AddressVO addressVO);

    // 기존 주소 삭제
    int addressDelete(int addressId, int userId);

    // 사용자 주소 목록 조회
    List<AddressVO> getAddressList(int userId);
}
