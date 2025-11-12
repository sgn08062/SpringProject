package com.example.myFarm.address;

import com.example.myFarm.command.AddressVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper {

    int addressInsertDB(AddressVO addressVO); // DB에 새로운 주소 추가

    int addressUpdateDB(AddressVO addressVO); // DB에 기존의 주소 수정

    int addressDeleteDB(int addId);

    List<AddressVO> getAddressListDB(int userId); // 사용자 주소 목록 조회


}
