package com.example.myFarm.address;


import com.example.myFarm.command.AddressVO;

public interface AddressService {

    int addressInsert(AddressVO addressVO); // 새로운 주소 등록

    int addressUpdate(AddressVO addressVO); // 기존 주소 수정

    int addressDelete(int addId); // 기존 주소 삭제

}
