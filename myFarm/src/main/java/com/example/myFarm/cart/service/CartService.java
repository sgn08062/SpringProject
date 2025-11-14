package com.example.myFarm.cart.service;

import com.example.myFarm.cart.dto.CartDTO;
import com.example.myFarm.cart.dto.CartViewDTO;
import com.example.myFarm.cart.mapper.CartMapper;
// [신규] 다른 분들이 만든 새로운 Member 패키지의 DTO와 Mapper를 import
import com.example.myFarm.member.dto.MemberDTO;
import com.example.myFarm.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드를 자동으로 주입
public class CartService {

    private final CartMapper cartMapper;
    // [신규] 다른 분들이 만든 MemberMapper를 주입받음
    private final MemberMapper memberMapper;

    /**
     * [신규 헬퍼 메서드]
     * Controller에서 받은 loginId를 실제 userId(Long)로 변환합니다.
     */
    private Long getUserId(String loginId) {
        // 새로운 MemberMapper를 사용하여 USERS 테이블에서 사용자 정보 조회
        MemberDTO member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾을 수 없습니다. loginId: " + loginId));
        return member.getUserId();
    }


    /**
     * [v12] 장바구니 목록 조회
     * @param loginId (Controller로부터 받은 String ID)
     * @return
     */
    public List<CartViewDTO> getCartItems(String loginId) {
        // [수정] loginId를 userId로 변환
        Long userId = getUserId(loginId);

        // Mapper가 SHOP과 JOIN하여 이름/가격을 모두 가져옴
        return cartMapper.findCartItemsByUserId(userId);
    }

    /**
     * [v12] 장바구니에 아이템 추가 (또는 수량 변경)
     *
     * @param loginId (Controller로부터 받은 String ID)
     * @param itemId (script.js에서 보낸 productId)
     * @param quantity (추가할 수량, 음수일 수 있음)
     */
    @Transactional
    public void addItemToCart(String loginId, Long itemId, int quantity) {
        // [수정] loginId를 userId로 변환
        Long userId = getUserId(loginId);

        // 1. 이 상품이 이미 장바구니에 있는지 확인
        Optional<CartDTO> existingItem = cartMapper.findCartItemByCompositeKey(userId, itemId);

        if (existingItem.isPresent()) {
            // 2. (A) 이미 있는 경우: 수량 업데이트
            CartDTO item = existingItem.get();
            int newAmount = item.getAmount() + quantity;

            if (newAmount <= 0) {
                // 3. (A-1) 새 수량이 0 이하면: 아이템 삭제
                cartMapper.deleteCartItem(userId, itemId);
            } else {
                // 3. (A-2) 새 수량이 0보다 크면: 수량 업데이트
                item.setAmount(newAmount);
                cartMapper.updateCartItemAmount(item);
            }

        } else {
            // 2. (B) 없는 경우:
            if (quantity > 0) {
                // 3. (B-1) 추가할 수량이 양수일 때만: 새 아이템 삽입
                CartDTO newItem = new CartDTO(userId, itemId, quantity);
                cartMapper.insertCartItem(newItem);
            }
            // (추가할 수량이 음수면 아무것도 하지 않음)
        }
    }

    /**
     * [v12] 장바구니 아이템 완전 삭제
     *
     * @param loginId (Controller로부터 받은 String ID)
     * @param itemId
     */
    @Transactional
    public void deleteCartItem(String loginId, Long itemId) {
        // [수정] loginId를 userId로 변환
        Long userId = getUserId(loginId);

        cartMapper.deleteCartItem(userId, itemId);
    }
}