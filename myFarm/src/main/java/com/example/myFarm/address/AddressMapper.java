package com.example.myFarm.address;

import com.example.myFarm.command.AddressVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddressMapper {

    int addressInsertDB(AddressVO addressVO); // DB에 새로운 주소 추가

    int addressUpdateDB(AddressVO addressVO); // DB에 기존의 주소 수정

    int addressDeleteDB(@Param("addressId") int addressId,
                        @Param("userId") int userId); // DB에 기존의 주소 삭제

    List<AddressVO> getAddressListDB(int userId); // 사용자 주소 목록 조회


}
