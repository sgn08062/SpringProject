package com.example.myFarm.controller;

import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.user.UserService;
import com.example.myFarm.user.DummyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DummyService dummyService;


    private int getCurrentUserId(HttpSession session) {
        // ⭐ 세션 키를 "userId"로 변경하여 로그인 로직과 일치시킵니다.
        final String USER_ID_SESSION_KEY = "userId";

        Object userIdObject = session.getAttribute(USER_ID_SESSION_KEY);

        if (userIdObject != null) {
            try {
                // 세션에서 값을 찾은 경우 반환
                return (Integer) userIdObject;
            } catch (ClassCastException e) {
                System.err.println("세션에 저장된 사용자 ID가 정수형이 아닙니다: " + userIdObject.getClass().getName());
                // 타입 캐스팅 오류 시 (로그인 로직 문제) 1번 사용자 ID 반환
                return 1;
            }
        }
        // ⭐ 세션에 ID가 없을 경우 (미로그인 상태) 1번 사용자 ID 반환 (DB 외래 키 오류 방지)
        return 1;
    }

    @GetMapping("/list")
    public String productList(Model model) {
        List<ItemVO> itemList = dummyService.getAllShopItems();
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("itemList", itemList);
        return "user/list";
    }

    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        ItemVO itemDetail = dummyService.getShopItemDetail(itemId.longValue());

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("item", itemDetail);

        return "user/detail";
    }

    @GetMapping("/cart")
    public String getCart(Model model, HttpSession session) {
        model.addAttribute("isLoggedIn", true);
        int userId = getCurrentUserId(session);
        List<CartVO> cartList = userService.getCartList(userId);
        model.addAttribute("cartList", cartList);
        return "user/cart";
    }

    @PostMapping("/pushCart")
    public String pushCart(@RequestParam Integer itemId, @RequestParam(defaultValue = "1") int amount, RedirectAttributes ra, HttpSession session) {
        int userId = getCurrentUserId(session);
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

    @PostMapping("/cart/update")
    @ResponseBody
    public Map<String, Object> updateCartAmount(@RequestParam Integer itemId, @RequestParam int amount, HttpSession session) {
        int userId = getCurrentUserId(session);
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

    @PostMapping("/cart/delete/{itemId}")
    @ResponseBody
    public Map<String, Object> deleteCart(@PathVariable("itemId") Integer itemId, HttpSession session) {
        int userId = getCurrentUserId(session);
        try {
            userService.deleteCartItem(userId, itemId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 항목 삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/orderList")
    public String getOrderList(Model model, @RequestParam(required = false) String successMessage, @RequestParam(required = false) String errorMessage, HttpSession session) {

        List<OrderVO> orderList = userService.getOrderList(getCurrentUserId(session));

        model.addAttribute("orderList", orderList);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "user/orderList";
    }

    @GetMapping("/order")
    public String getOrderPage(
            @RequestParam(name = "selectedItems", required = false) List<Integer> selectedItems,
            @RequestParam Map<String, String> allParams,
            Model model,
            RedirectAttributes ra,
            HttpSession session) {

        int userId = getCurrentUserId(session);

        if (selectedItems == null || selectedItems.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "주문할 상품이 없습니다.");
            return "redirect:/user/cart";
        }

        List<CartVO> cartList = userService.getCartList(userId);

        List<ItemVO> checkoutItems = cartList.stream()
                .filter(cart -> selectedItems.contains(cart.getItemId()))
                .map(cart -> {
                    String amountKey = "itemAmount_" + cart.getItemId();
                    int finalAmount = cart.getAmount();

                    if (allParams.containsKey(amountKey)) {
                        try {
                            finalAmount = Math.max(1, Integer.parseInt(allParams.get(amountKey)));
                        } catch (NumberFormatException ignored) {}
                    }

                    ItemVO item = new ItemVO();
                    item.setItemId(cart.getItemId());
                    item.setItemName(cart.getItemName());
                    item.setPrice(cart.getPrice());
                    item.setOrderAmount(finalAmount);
                    item.setStockAmount(cart.getStockAmount());

                    return item;
                })
                .filter(item -> item.getStockAmount() > 0)
                .collect(Collectors.toList());

        if (checkoutItems.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "선택하신 상품을 찾을 수 없거나 이미 품절된 상품이 포함되어 주문할 수 없습니다.");
            return "redirect:/user/cart";
        }

        int totalPrice = checkoutItems.stream()
                .mapToInt(item -> item.getPrice() * item.getOrderAmount())
                .sum();

        model.addAttribute("checkoutItems", checkoutItems);
        model.addAttribute("totalPrice", totalPrice);

        AddressVO defaultAddress = userService.getDefaultAddress(userId);
        model.addAttribute("defaultAddress", defaultAddress);

        List<AddressVO> otherAddresses = userService.getOtherAddresses(userId);
        model.addAttribute("otherAddresses", otherAddresses);

        return "user/order";
    }


    @PostMapping("/placeOrder")
    public String placeOrder(
            @ModelAttribute OrderVO order,
            @RequestParam(required = false) String newRecipientName,
            @RequestParam(required = false) String newPhone,
            @RequestParam(required = false) String newAddressInput,
            @RequestParam(required = false) String newAddressNameInput,
            @RequestParam Map<String, String> itemAmounts,
            RedirectAttributes ra,
            HttpSession session) {

        int userId = getCurrentUserId(session);
        order.setUserId(userId);

        // ⭐ 1. addressId == 0 (새 주소 입력) 처리
        if (order.getAddressId() == 0) {

            // 새 주소 입력 필수 필드 체크
            if (newAddressInput == null || newAddressInput.trim().isEmpty() ||
                    newRecipientName == null || newRecipientName.trim().isEmpty() ||
                    newPhone == null || newPhone.trim().isEmpty()) {

                ra.addFlashAttribute("errorMessage", "새 주소 입력 시 모든 필수 정보를 입력해주세요 (수령인, 연락처, 주소).");
                return "redirect:/user/order"; // /user/cart 대신 /user/order로 리다이렉트
            }

            // 새 주소 정보를 AddressVO로 변환 및 저장
            AddressVO newAddress = new AddressVO();
            newAddress.setUserId(userId);
            newAddress.setAddress(newAddressInput);
            newAddress.setAddressName(newAddressNameInput != null && !newAddressNameInput.isEmpty() ? newAddressNameInput : "새 주소");
            newAddress.setRecipientName(newRecipientName);
            newAddress.setRecipientPhone(newPhone);

            try {
                // AddressVO를 DB에 저장하고, 생성된 ID를 받아옵니다.
                userService.saveAddress(newAddress);
            } catch (Exception e) {
                ra.addFlashAttribute("errorMessage", "새 배송지 저장 중 오류가 발생했습니다.");
                e.printStackTrace();
                return "redirect:/user/order";
            }

            // OrderVO에 새로 생성된 addressId를 설정하여 Service Layer로 전달
            order.setAddressId(newAddress.getAddressId());
        }

        // ⭐ 2. 기존 주소 선택 (addressId > 0) 처리
        // addressId가 OrderVO에 이미 설정되어 있으므로 별도 로직은 불필요합니다.
        // Service Layer (UserServiceImpl)에서 addressId를 통해 ORDERS 테이블에 저장될 최종 주소 스냅샷을 결정합니다.

        // ⭐ 3. 기존 주소 유효성 검사 로직 제거
        /* else {
            if (order.getAddress() == null || order.getAddress().trim().isEmpty() ||
                    order.getRecipientName() == null || order.getRecipientName().trim().isEmpty() ||
                    order.getPhone() == null || order.getPhone().trim().isEmpty()) {

                ra.addFlashAttribute("errorMessage", "선택된 기본 배송지 정보가 유효하지 않습니다. 주소 관리를 확인해주세요.");
                return "redirect:/user/cart";
            }
        }
        */

        order.setStatus("결제 완료");

        try {
            Long orderId = userService.placeOrder(order, itemAmounts);

            ra.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다! (주문 ID: " + orderId + ")");
            return "redirect:/user/orderList";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/user/orderList";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "시스템 오류로 주문에 실패했습니다.");
            e.printStackTrace();
            return "redirect:/user/orderList";
        }
    }


    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable("id") Long orderId, Model model, HttpSession session) {

        OrderVO orderData = userService.getOrderDetail(orderId, getCurrentUserId(session));

        if (orderData == null) {
            return "redirect:/user/orderList";
        }

        model.addAttribute("order", orderData);
        List<ItemVO> orderItems = userService.getOrderItems(orderId);
        model.addAttribute("orderItems", orderItems);

        return "user/orderDetail";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes ra, HttpSession session) {
        try {
            userService.cancelOrder(orderId, getCurrentUserId(session));
            ra.addFlashAttribute("successMessage", orderId + "번 주문이 취소되었으며, 재고가 복구되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "주문 취소 중 시스템 오류가 발생했습니다.");
        }

        return "redirect:/user/orderList";
    }

    @PostMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes ra, HttpSession session) {
        userService.deleteAddress(addressId, getCurrentUserId(session));
        ra.addFlashAttribute("successMessage", addressId + "번 배송지가 삭제되었습니다.");
        return "redirect:/user/orderList";
    }
}