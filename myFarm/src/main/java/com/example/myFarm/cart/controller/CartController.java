package com.example.myFarm.cart.controller;

import com.example.myFarm.cart.dto.CartItemDTO;
import com.example.myFarm.cart.service.CartService; // <-- [수정] 이 줄이 추가되었습니다.
import lombok.Data;                                 // <-- [수정] 이 줄이 추가되었습니다.
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import com.example.myFarm.user.UserDetails; // (로그인 기능 연동 시)

@RestController // 이 클래스는 HTML 페이지가 아닌 JSON/XML 데이터를 반환합니다.
@RequiredArgsConstructor
@RequestMapping("/api/cart") // 이 컨트롤러의 모든 메서드는 /api/cart 로 시작합니다.
public class CartController {

    private final CartService cartService;

    /**
     * [GET /api/cart]
     * 현재 로그인한 사용자의 장바구니 목록을 조회합니다.
     */
    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        // 🚨 중요:
        // 실제로는 Spring Security의 @AuthenticationPrincipal 어노테이션 등으로
        // 로그인한 사용자의 ID (memberId)를 가져와야 합니다.
        // 지금은 테스트를 위해 임시로 '1L' (1번 회원)을 사용합니다.
        Long currentMemberId = 1L; // <<-- (임시)

        List<CartItemDTO> cartItems = cartService.getCartItems(currentMemberId);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * [POST /api/cart]
     * 장바구니에 아이템을 추가합니다.
     */
    // JavaScript가 보낼 JSON 요청의 형식을 담을 DTO (이너 클래스로 간단히 만듦)
    @Data // Lombok
    static class AddItemRequest {
        private Long productId;
        private int quantity;
    }

    @PostMapping
    public ResponseEntity<String> addItemToCart(@RequestBody AddItemRequest request) {
        // 🚨 위와 동일하게, 1L은 임시 ID입니다.
        Long currentMemberId = 1L; // <<-- (임시)

        cartService.addItemToCart(
                currentMemberId,
                request.getProductId(),
                request.getQuantity()
        );

        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }

    /**
     * [DELETE /api/cart/{itemId}]
     * 장바구니에서 특정 아이템을 삭제합니다.
     * {itemId} 부분은 URL을 통해 동적으로 변합니다.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable("itemId") Long cartItemId) {

        // (보안) 실제로는 이 cartItemId가 현재 로그인한 사용자의 장바구니에
        // 속한 것이 맞는지 확인하는 로직이 필요합니다.

        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }
}