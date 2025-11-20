/*
package com.example.myFarm.controller;

import com.example.myFarm.cart.CartService;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String getCart(Model model, HttpSession session, RedirectAttributes ra) {
        model.addAttribute("isLoggedIn", true);
        int userId = SessionUtil.getCurrentUserId(session);

        List<CartVO> cartList = cartService.getCartList(userId);

        if (cartList == null || cartList.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "🛒 장바구니에 담긴 상품이 없습니다. 상품 목록에서 상품을 담아주세요.");
            return "redirect:/user/list";
        }

        model.addAttribute("cartList", cartList);
        // 🚨 수정 완료: 뷰 경로를 "cart/cart"로 변경했습니다.
        return "cart/cart";
    }

    @PostMapping("/pushCart")
    public String pushCart(@RequestParam Integer itemId, @RequestParam(defaultValue = "1") int amount, RedirectAttributes ra, HttpSession session) {
        int userId = SessionUtil.getCurrentUserId(session);
        CartVO cart = new CartVO();
        cart.setUserId(userId);
        cart.setItemId(itemId);
        cart.setAmount(amount);

        try {
            cartService.addCartItem(cart);
            ra.addFlashAttribute("successMessage", itemId + "번 상품이 장바구니에 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "장바구니 추가 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartAmount(@RequestParam Integer itemId, @RequestParam int amount, HttpSession session) {
        int userId = SessionUtil.getCurrentUserId(session);
        if (amount < 1) { amount = 1; }

        CartVO cart = new CartVO();
        cart.setUserId(userId);
        cart.setItemId(itemId);
        cart.setAmount(amount);

        try {
            cartService.updateCartItem(cart);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 수량 변경 실패: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{itemId}")
    @ResponseBody
    public Map<String, Object> deleteCart(@PathVariable("itemId") Integer itemId, HttpSession session) {
        int userId = SessionUtil.getCurrentUserId(session);
        try {
            cartService.deleteCartItem(userId, itemId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 항목 삭제 실패: " + e.getMessage());
        }
    }
}*/
