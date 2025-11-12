package com.example.myFarm.cart.service;

import com.example.myFarm.cart.dto.CartDTO;
import com.example.myFarm.cart.dto.CartItemDTO;
import com.example.myFarm.cart.mapper.CartMapper;
import lombok.RequiredArgsConstructor; // Lombok 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service // Spring이 이 클래스를 '서비스'로 인식하게 함
@RequiredArgsConstructor // final이 붙은 필드의 생성자를 자동으로 만들어줍니다 (Autowired 대신)
public class CartService {

    // final로 선언하고 @RequiredArgsConstructor를 쓰면
    // Spring이 알아서 CartMapper를 주입해줍니다. (생성자 주입)
    private final CartMapper cartMapper;

    /**
     * 회원 ID로 장바구니 조회 (없으면 자동 생성)
     * 이 메서드는 다른 로직에서 "일단 장바구니부터 가져와"라고 할 때 쓰입니다.
     */
    @Transactional // DB 상태가 바뀔 수 있으므로 트랜잭션 처리
    public CartDTO getOrCreateCart(Long memberId) {
        // 1. memberId로 장바구니가 있는지 DB에서 찾아봅니다.
        Optional<CartDTO> existingCart = cartMapper.findCartByMemberId(memberId);

        if (existingCart.isPresent()) {
            // 2. (A) 장바구니가 이미 있으면, 그걸 반환합니다.
            return existingCart.get();
        } else {
            // 3. (B) 장바구니가 없으면, 새로 만들어서 DB에 저장합니다.
            CartDTO newCart = new CartDTO();
            newCart.setMemberId(memberId);
            cartMapper.createCart(newCart); // DB에 INSERT
            // (createCart 쿼리에서 useGeneratedKeys=true를 썼기 때문에
            //  newCart 객체에 방금 생성된 cart_id가 자동으로 담깁니다)
            return newCart;
        }
    }

    /**
     * [핵심] 장바구니에 아이템 추가
     *
     * @param memberId  로그인한 고객 ID
     * @param productId 추가할 상품 ID
     * @param quantity  추가할 수량
     */
    @Transactional
    public void addItemToCart(Long memberId, Long productId, int quantity) {
        // 1. 이 고객의 장바구니가 있는지 확인 (없으면 자동 생성)
        CartDTO cart = getOrCreateCart(memberId);
        Long cartId = cart.getCartId();

        // 2. 이 장바구니(cartId)에 이 상품(productId)이
        //    '이미' 담겨 있는지 확인합니다.
        Optional<CartItemDTO> existingItem = cartMapper.findCartItemByCartIdAndProductId(cartId, productId);

        if (existingItem.isPresent()) {
            // 3. (A) 이미 있는 경우:
            //    기존 수량에 새로 추가할 수량을 더해서 수량 'UPDATE'
            CartItemDTO item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartMapper.updateCartItemQuantity(item); // UPDATE 쿼리 실행
        } else {
            // 4. (B) 없는 경우:
            //    새 CartItemDTO를 만들어서 DB에 'INSERT'
            CartItemDTO newItem = new CartItemDTO();
            newItem.setCartId(cartId);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            cartMapper.insertCartItem(newItem); // INSERT 쿼리 실행
        }
    }

    /**
     * 장바구니 목록 조회
     * @param memberId 고객 ID
     * @return 장바구니 아이템 리스트
     */
    public List<CartItemDTO> getCartItems(Long memberId) {
        // 1. 고객의 장바구니 ID를 찾습니다.
        CartDTO cart = getOrCreateCart(memberId);
        // 2. 해당 장바구니의 모든 아이템을 조회합니다.
        return cartMapper.findCartItemsByCartId(cart.getCartId());
    }

    /**
     * 장바구니 아이템 삭제
     * @param cartItemId 삭제할 아이템의 고유 ID
     */
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        cartMapper.deleteCartItem(cartItemId);
    }
}