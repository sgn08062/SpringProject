package com.example.myFarm.controller;

import com.example.myFarm.command.CartVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.user.UserService;
import com.example.myFarm.user.DummyService;
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

    private int getCurrentUserId() {
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
    public String getCart(Model model) {
        model.addAttribute("isLoggedIn", true);
        int userId = getCurrentUserId();
        List<CartVO> cartList = userService.getCartList(userId);
        model.addAttribute("cartList", cartList);
        return "user/cart";
    }

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

    @PostMapping("/cart/update")
    @ResponseBody
    public Map<String, Object> updateCartAmount(@RequestParam Integer itemId, @RequestParam int amount) {
        // ... (로직 유지) ...
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

    @PostMapping("/cart/delete/{itemId}")
    @ResponseBody
    public Map<String, Object> deleteCart(@PathVariable("itemId") Integer itemId) {
        // **[유지] 장바구니 삭제는 AJAX로 처리되므로, DELETE 대신 POST를 유지하여 호환성 확보 가능**
        // 다만 RESTful 원칙을 따르려면 @DeleteMapping으로 변경하고 JS를 수정해야 함.
        // 현재는 'POST'를 유지하여 기존 JS 코드를 살립니다.
        int userId = getCurrentUserId();
        try {
            userService.deleteCartItem(userId, itemId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 항목 삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/orderList")
    public String getOrderList(Model model, @RequestParam(required = false) String successMessage, @RequestParam(required = false) String errorMessage) {

        List<OrderVO> orderList = userService.getOrderList(getCurrentUserId());
        List<AddressVO> addressList = userService.getAddressList(getCurrentUserId());

        model.addAttribute("orderList", orderList);
        model.addAttribute("addressList", addressList);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "user/orderList";
    }

    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable("id") Long orderId, Model model) {

        OrderVO orderData = userService.getOrderDetail(orderId, getCurrentUserId());

        if (orderData == null) {
            return "redirect:/user/orderList";
        }

        model.addAttribute("order", orderData);
        List<ItemVO> orderItems = userService.getOrderItems(orderId);
        model.addAttribute("orderItems", orderItems);

        return "user/orderDetail";
    }

    @PostMapping("/order/cancel/{id}") // **[유지] PATCH 매핑**
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes ra) {
        // ... (로직 유지) ...
        try {
            userService.cancelOrder(orderId, getCurrentUserId());
            ra.addFlashAttribute("successMessage", orderId + "번 주문이 취소되었으며, 재고가 복구되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "주문 취소 중 시스템 오류가 발생했습니다.");
        }

        return "redirect:/user/orderList";
    }

    @PostMapping("/address")
    public String saveAddress(@ModelAttribute AddressVO addressForm, RedirectAttributes ra) {
        addressForm.setUserId(getCurrentUserId());

        userService.saveAddress(addressForm);

        String message;
        if (addressForm.getAddId() == null || addressForm.getAddId() == 0) {
            message = addressForm.getAddName() + " 배송지가 새로 등록되었습니다.";
        } else {
            message = addressForm.getAddName() + " 배송지 정보가 수정되었습니다.";
        }

        ra.addFlashAttribute("successMessage", message);
        return "redirect:/user/orderList";
    }

    @PostMapping("/address/delete/{id}") // **[수정] POST -> DELETE**
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes ra) {
        userService.deleteAddress(addressId, getCurrentUserId());
        ra.addFlashAttribute("successMessage", addressId + "번 배송지가 삭제되었습니다.");
        return "redirect:/user/orderList";
    }

    @GetMapping("/order")
    public String getOrderPage(
            @RequestParam(name = "selectedItems", required = false) List<Integer> selectedItems,
            @RequestParam Map<String, String> allParams,
            Model model,
            RedirectAttributes ra) {

        int userId = getCurrentUserId();

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

        return "user/order";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam(required = false) Long addressId,
            @RequestParam(required = false) String newAddress,
            @RequestParam(required = false) String newAddressName,
            @RequestParam Map<String, String> itemAmounts,
            RedirectAttributes ra) {

        int userId = getCurrentUserId();
        AddressVO selectedAdd = null;

        if (newAddress != null && !newAddress.trim().isEmpty()) {
            AddressVO newAddressForm = new AddressVO();
            newAddressForm.setUserId(userId);
            newAddressForm.setAddress(newAddress);
            newAddressForm.setAddName(newAddressName != null && !newAddressName.isEmpty() ? newAddressName : "새 주소");

            userService.saveAddress(newAddressForm);
            selectedAdd = newAddressForm;

        } else if (addressId != null && addressId > 0) {
            selectedAdd = userService.getAddressDetail(addressId, userId);
        }

        // **[수정된 핵심 로직] 유효성 검사 활성화 및 강화**
        // selectedAdd 객체가 null이거나, 주소 필드가 null 또는 비어있으면 에러 반환
        if (selectedAdd == null || selectedAdd.getAddress() == null || selectedAdd.getAddress().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "유효한 배송지 정보를 선택하거나 입력해주세요.");
            // 배송지 문제 시, 다시 주문 페이지로 리다이렉트
            return "redirect:/user/order";
        }


        OrderVO order = new OrderVO();
        order.setUserId(userId);
        order.setStatus("주문 대기");
        // 유효성 검사를 통과했으므로 selectedAdd.getAddress()는 안전하게 접근 가능
        order.setAddress(selectedAdd.getAddress());

        try {
            Long orderId = userService.placeOrder(order, itemAmounts);

            ra.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다! (주문 ID: " + orderId + ")");
            // 요청하신 대로 주문 목록으로 리다이렉트
            return "redirect:/user/orderList";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            // 재고 등 문제 시, 주문 목록으로 리다이렉트하여 재확인 유도 (이전 코드 유지)
            return "redirect:/user/orderList";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "시스템 오류로 주문에 실패했습니다.");
            e.printStackTrace();
            // 기타 시스템 오류 시, 주문 목록으로 리다이렉트 (이전 코드 유지)
            return "redirect:/user/orderList";
        }
    }

    @GetMapping("/order/success/{orderId}")
    public String getOrderSuccessPage(@PathVariable("orderId") Long orderId, Model model, RedirectAttributes ra) {
        int userId = getCurrentUserId();

        OrderVO orderData = userService.getOrderDetail(orderId, userId);

        if (orderData == null) {
            ra.addFlashAttribute("errorMessage", "유효하지 않은 주문 정보입니다.");
            return "redirect:/user/orderList";
        }

        List<ItemVO> orderItems = userService.getOrderItems(orderId);

        model.addAttribute("order", orderData);
        model.addAttribute("orderItems", orderItems);

        return "user/orderDetail";
    }
}