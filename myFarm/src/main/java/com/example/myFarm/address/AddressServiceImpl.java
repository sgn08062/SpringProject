package com.example.myFarm.address;

import com.example.myFarm.command.AddressVO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("addressService")
public class AddressServiceImpl implements AddressService {
    
    @Autowired
    @Qualifier("addressMapper")
    private AddressMapper addressMapper;

    // 새로운 주소 등록
    @Override
    public int addressInsert(AddressVO addressVO) {
        return addressMapper.addressInsertDB(addressVO);
    }
    
    // 기존 주소 수정
    @Override
    public int addressUpdate(AddressVO addressVO) {
        return addressMapper.addressUpdateDB(addressVO);
    }
    
    // 기존 주소 삭제
    @Override
    public int addressDelete(int addressId, int userId) {
        return addressMapper.addressDeleteDB(addressId, userId);
    }

    // 사용자 주소 조회
    @Override
    public List<AddressVO> getAddressList(int userId) {
        return addressMapper.getAddressListDB(userId);
    }
}
