package com.example.myFarm.cart.controller;

import com.example.myFarm.cart.dto.CartViewDTO;
import com.example.myFarm.cart.service.CartService;
import com.example.myFarm.user.dto.MemberDTO;
import com.example.myFarm.user.mapper.MemberMapper;
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
    private final MemberMapper memberMapper; // (사용자 ID를 조회하기 위해)

    /**
     * [내부 공용 메서드]
     * Spring Security의 UserDetails에서 USERS.USER_ID (Long)를 조회
     */
    private Long getMemberIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        String loginId = userDetails.getUsername();
        MemberDTO member = memberMapper.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
        return member.getUserId();
    }

    /**
     * [GET /api/cart]
     * [v12] 현재 로그인한 사용자의 장바구니 목록 조회 (JOIN된 결과)
     */
    @GetMapping
    public ResponseEntity<List<CartViewDTO>> getCartItems(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Long currentUserId = getMemberIdFromUserDetails(userDetails);
            List<CartViewDTO> cartItems = cartService.getCartItems(currentUserId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
        try {
            Long currentUserId = getMemberIdFromUserDetails(userDetails);

            // Service가 수량(양수/음수)에 따라 추가/수정/삭제를 알아서 처리
            cartService.addItemToCart(
                    currentUserId,
                    request.getProductId(), // -> itemId
                    request.getQuantity()
            );
            return ResponseEntity.ok("장바구니가 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
    }

    /**
     * [DELETE /api/cart/{itemId}]
     * [v12] 장바구니에서 아이템 '완전 삭제'
     * (script.js의 '-' 버튼이 아니라, 'X' 버튼용)
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteCartItem(
            @PathVariable("itemId") Long itemId, // (상품 ID)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Long currentUserId = getMemberIdFromUserDetails(userDetails);

            // Service의 완전 삭제 메서드 호출
            cartService.deleteCartItem(currentUserId, itemId);
            return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
    }
}