package com.example.myFarm.controller;

// 기존 임포트 유지
import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.AddressVO;

import com.example.myFarm.command.DummyVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.user.UserService;
import com.example.myFarm.user.DummyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DummyService dummyService;

    private int getCurrentUserId() {
        return 1;
    }

    // 1. 상품 목록
    @GetMapping("/list")
    public String productList(Model model) {
        // 💡 타입 수정: DummyService의 반환 타입인 독립 클래스 ItemVO 사용
        List<ItemVO> itemList = dummyService.getAllShopItems();
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("itemList", itemList);
        return "user/list";
    }

    // 2. 상품 상세
    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        // 💡 타입 수정: DummyVO.ItemVO를 독립 클래스 ItemVO로 수정
        ItemVO itemDetail = dummyService.getShopItemDetail(itemId.longValue());

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("item", itemDetail);

        return "user/detail";
    }

    // 3. 장바구니 조회 (UserService 유지)
    @GetMapping("/cart")
    public String getCart(Model model) {
        model.addAttribute("isLoggedIn", true);
        int userId = getCurrentUserId();
        List<CartVO> cartList = userService.getCartList(userId);
        model.addAttribute("cartList", cartList);
        return "user/cart";
    }

    // 4. 장바구니 추가 (UserService 유지)
    @PostMapping("/pushCart")
    public String pushCart(@RequestParam Integer itemId, @RequestParam(defaultValue = "1") int amount, RedirectAttributes ra) {
        int userId = getCurrentUserId();
        CartVO cart = new CartVO();
        cart.setUserId(userId);
        cart.setItemId(itemId);
        cart.setAmount(amount);

        try {
            userService.addCartItem(cart);
            ra.addFlashAttribute("successMessage", itemId + "번 상품이 장바구니에 추가되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "장바구니 추가 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/user/cart";
    }

    // 5. 장바구니 수량 변경 (UserService 유지)
    @PostMapping("/cart/update")
    @ResponseBody
    public Map<String, Object> updateCartAmount(@RequestParam Integer itemId, @RequestParam int amount) {
        int userId = getCurrentUserId();
        if (amount < 1) { amount = 1; }

        CartVO cart = new CartVO();
        cart.setUserId(userId);
        cart.setItemId(itemId);
        cart.setAmount(amount);

        try {
            userService.updateCartItem(cart);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 수량 변경 실패: " + e.getMessage());
        }
    }

    // 6. 장바구니 항목 삭제 (UserService 유지)
    @PostMapping("/cart/delete/{itemId}")
    @ResponseBody
    public Map<String, Object> deleteCart(@PathVariable("itemId") Integer itemId) {
        int userId = getCurrentUserId();
        try {
            userService.deleteCartItem(userId, itemId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 항목 삭제 실패: " + e.getMessage());
        }
    }

    // 7. 주문 및 관리 페이지 (UserService 유지)
    @GetMapping("/order")
    public String getOrder(Model model, @RequestParam(required = false) String successMessage) {

        List<OrderVO> orderList = userService.getOrderList(getCurrentUserId());
        List<AddressVO> addressList = userService.getAddressList(getCurrentUserId());

        model.addAttribute("orderList", orderList);
        model.addAttribute("addressList", addressList);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        return "user/order";
    }

    // 8. 주문 상세 조회
    @GetMapping("/order/{id}")
    public String getOrder(@PathVariable("id") Long orderId, Model model) {

        OrderVO orderData = userService.getOrderDetail(orderId);

        if (orderData == null) {
            return "redirect:/user/order";
        }

        model.addAttribute("order", orderData);

        // 💡 타입 수정: DummyVO.ItemVO를 독립 클래스 ItemVO로 수정
        List<ItemVO> orderItems = dummyService.getOrderItems(orderId);
        model.addAttribute("orderItems", orderItems);

        return "user/orderDetail";
    }

    // 9. 주문 취소 (UserService 유지)
    @PatchMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes ra) {

        userService.cancelOrder(orderId, getCurrentUserId());
        ra.addFlashAttribute("successMessage", orderId + "번 주문이 취소되었습니다.");
        return "redirect:/user/order?tab=orderListTab";
    }

    // 10. 배송지 저장/수정 (UserService 유지)
    @PostMapping("/address")
    public String saveAddress(@ModelAttribute AddressVO addressForm, RedirectAttributes ra) {

        userService.saveAddress(addressForm);

        String message;
        if (addressForm.getAddId() == null || addressForm.getAddId() == 0) {
            message = addressForm.getAddName() + " 배송지가 새로 등록되었습니다.";
        } else {
            message = addressForm.getAddName() + " 배송지 정보가 수정되었습니다.";
        }

        ra.addFlashAttribute("successMessage", message);
        return "redirect:/user/order?tab=addressManageTab";
    }

    // 11. 배송지 삭제 (UserService 유지)
    @PostMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes ra) {
        userService.deleteAddress(addressId, getCurrentUserId());
        ra.addFlashAttribute("successMessage", addressId + "번 배송지가 삭제되었습니다.");
        return "redirect:/user/order?tab=addressManageTab";
    }

    // 12. 결제 페이지
    @GetMapping("/checkout")
    public String getCheckoutPage(Model model) {

        // 💡 타입 수정: DummyVO.ItemVO를 독립 클래스 ItemVO로 수정
        List<ItemVO> checkoutItems = dummyService.getCheckoutItems(getCurrentUserId());

        if (checkoutItems == null) {
            checkoutItems = Collections.emptyList();
        }

        // Stream 로직은 ItemVO 독립 클래스 타입을 가정하고 price, amount 필드를 사용
        int totalPrice = checkoutItems.stream()
                .mapToInt(item -> item.getPrice() * item.getAmount())
                .sum();

        model.addAttribute("checkoutItems", checkoutItems);

        model.addAttribute("totalPrice", totalPrice);

        AddressVO defaultAddress = userService.getDefaultAddress(getCurrentUserId());

        model.addAttribute("defaultAddress", defaultAddress);
        return "user/order";
    }

    // 13. 주문 확정 (UserService 유지)
    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam Long addressId, // 기본 배송지 ID (default 선택 시 사용)
            @RequestParam(required = false) String newAddress, // 새로운 주소 (new 선택 시 사용)
            @RequestParam(required = false) String newPhone,   // 새로운 연락처 (new 선택 시 사용)
            @RequestParam(required = false) String newAddressName, // 새로운 배송지명 (new 선택 시 사용)
            RedirectAttributes ra) {

        int userId = getCurrentUserId();
        Long finalAddressId = addressId;
        String finalPhone = "";

        AddressVO selectedAdd = null;

        // --- 1단계: 배송지 결정 및 등록 ---
        if (newAddress != null && !newAddress.trim().isEmpty()) {
            // "새로운 배송지 입력" 옵션 선택 시

            // 1-1. 새 배송지 정보를 AddressVO에 담아 저장
            AddressVO newAddressForm = new AddressVO();
            newAddressForm.setUserId(userId);
            newAddressForm.setAddress(newAddress);
            newAddressForm.setPhone(newPhone);
            newAddressForm.setAddName(newAddressName != null && !newAddressName.isEmpty() ? newAddressName : "새 주소");

            userService.saveAddress(newAddressForm); // 새 주소 등록 및 ID 획득

            finalAddressId = newAddressForm.getAddId(); // 새로 생성된 ID 사용
            finalPhone = newPhone;
            selectedAdd = newAddressForm;

        } else if (addressId != null && addressId > 0) {
            // "기본 배송지 사용" 옵션 선택 시
            selectedAdd = userService.getAddressDetail(addressId, userId);

            if (selectedAdd != null) {
                finalPhone = selectedAdd.getPhone();
            }
        }

        // --- 2단계: 유효성 검사 및 주문 진행 ---
        if (selectedAdd == null || finalAddressId == null || finalAddressId == 0) {
            ra.addFlashAttribute("errorMessage", "유효하지 않은 배송지 정보입니다.");
            return "redirect:/user/checkout";
        }

        // 3. OrderVO 생성
        OrderVO order = new OrderVO();
        order.setUserId(userId);
        order.setStatus("주문 대기");
        order.setAddress(selectedAdd.getAddress()); // 최종 결정된 주소 사용
        order.setPhone(finalPhone); // 최종 결정된 연락처 사용

        // 4. 주문 서비스 호출 및 예외 처리
        try {
            Long orderId = userService.placeOrder(order);
            ra.addFlashAttribute("successMessage", orderId + "번 주문이 성공적으로 완료되었습니다.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/user/checkout";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "시스템 오류로 주문에 실패했습니다.");
            return "redirect:/user/checkout";
        }

        // 5. 주문 성공 시 리다이렉트
        return "redirect:/user/order?tab=orderListTab";
    }

}