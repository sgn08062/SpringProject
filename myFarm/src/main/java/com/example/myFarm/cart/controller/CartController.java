package com.example.myFarm.cart.controller;

import com.example.myFarm.cart.dto.CartViewDTO;
import com.example.myFarm.cart.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

//    private final CartService cartService;
//    // [삭제] MemberMapper 제거함
//
//    // [삭제] getUserId 메서드도 제거함
//
//    @GetMapping
//    public ResponseEntity<List<CartViewDTO>> getCartItems(
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        // Service에 그냥 String(loginId)을 그대로 던짐
//        String loginId = userDetails.getUsername();
//        List<CartViewDTO> cartItems = cartService.getCartItems(loginId);
//
//        return ResponseEntity.ok(cartItems);
//    }
//
//    @Data
//    static class AddItemRequest {
//        private Long productId;
//        private int quantity;
//    }
//
//    @PostMapping
//    public ResponseEntity<String> addItemToCart(
//            @RequestBody AddItemRequest request,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }
//
//        String loginId = userDetails.getUsername();
//
//        try {
//            // Service에 String(loginId) 전달
//            cartService.addItemToCart(
//                    loginId,
//                    request.getProductId(),
//                    request.getQuantity()
//            );
//            return ResponseEntity.ok("장바구니가 업데이트되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{itemId}")
//    public ResponseEntity<String> deleteCartItem(
//            @PathVariable("itemId") Long itemId,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
//        }
//
//        String loginId = userDetails.getUsername();
//
//        try {
//            // Service에 String(loginId) 전달
//            cartService.deleteCartItem(loginId, itemId);
//            return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
}