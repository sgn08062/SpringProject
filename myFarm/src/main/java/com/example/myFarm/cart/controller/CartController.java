package com.example.myFarm.cart.controller;

import com.example.myFarm.cart.dto.CartViewDTO;
import com.example.myFarm.cart.service.CartService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    // [삭제] getMemberIdFromUserDetails 헬퍼 메서드 제거
    // (이 책임은 CartService로 이동합니다)

    /**
     * [GET /api/cart]
     * [v12] 현재 로그인한 사용자의 장바구니 목록 조회 (JOIN된 결과)
     */
    @GetMapping
    public ResponseEntity<List<CartViewDTO>> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // [수정] Service에 userId 대신 loginId(String)를 전달
        String loginId = userDetails.getUsername();
        List<CartViewDTO> cartItems = cartService.getCartItems(loginId);
        return ResponseEntity.ok(cartItems);
    }

    /**
     * [POST /api/cart]
     * [v12] 장바구니에 아이템 추가 (또는 수량 변경)
     */
    @Data
    static class AddItemRequest {
        // script.js가 'productId'로 보내는 것이 DB의 'itemId'임
        private Long productId;
        private int quantity;
    }

    @PostMapping
    public ResponseEntity<String> addItemToCart(
            @RequestBody AddItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // [수정] Service에 userId 대신 loginId(String)를 전달
        String loginId = userDetails.getUsername();

        try {
            // Service가 수량(양수/음수)에 따라 추가/수정/삭제를 알아서 처리
            cartService.addItemToCart(
                    loginId,
                    request.getProductId(), // -> itemId
                    request.getQuantity()
            );
            return ResponseEntity.ok("장바구니가 업데이트되었습니다.");
        } catch (Exception e) {
            // (예: loginId에 해당하는 유저가 없는 경우 등)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * [DELETE /api/cart/{itemId}]
     * [v12] 장바구니에서 아이템 '완전 삭제'
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteCartItem(
            @PathVariable("itemId") Long itemId, // (상품 ID)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // [수정] Service에 userId 대신 loginId(String)를 전달
        String loginId = userDetails.getUsername();

        try {
            cartService.deleteCartItem(loginId, itemId);
            return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}