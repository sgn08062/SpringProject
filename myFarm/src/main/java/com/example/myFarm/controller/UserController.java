package com.example.myFarm.controller;

// import com.example.myFarm.cart.CartService; // ⭐ 주석 처리: CartController, UserOrderController로 이동
import com.example.myFarm.command.CartVO;
// import com.example.myFarm.command.OrderVO; // ⭐ 주석 처리: UserOrderController로 이동
// import com.example.myFarm.command.AddressVO; // ⭐ 주석 처리: UserOrderController로 이동
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.ShopVO;
import com.example.myFarm.shop.AdminShopService;
import com.example.myFarm.user.UserService;
import com.example.myFarm.util.SessionUtil; // ⭐ 추가: SessionUtil 사용
// import com.example.myFarm.user.DummyService; // ❌ DUMMY SERVICE 주석 처리
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
    private final AdminShopService adminShopService;
    // private final CartService cartService; // ⭐ 주석 처리: 주문 로직 분리에 따라 주석 처리
    // private final DummyService dummyService; // ❌ DUMMY SERVICE 필드 주석 처리

    /* ❌ 삭제된 부분: SessionUtil로 로직 이동 및 통일
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
    */

    @GetMapping("/list")
    public String productList(Model model) {
        // List<ItemVO> itemList = dummyService.getAllShopItems(); // ❌ DUMMY METHOD 주석 처리
        // model.addAttribute("isLoggedIn", true);
        // model.addAttribute("itemList", itemList);
        // return "user/list";

        List<ShopVO> itemList = adminShopService.getAllItems();

        // ⭐ 더미 사용 부분 주석 처리 및 임시 처리
        //model.addAttribute("isLoggedIn", true);
        //model.addAttribute("itemList", List.of()); // 빈 리스트로 임시 대체

        model.addAttribute("isLoggedIn", true);
        model.addAttribute("itemList", itemList); // <-- List.of() 대신 itemList 변수 사용
        model.addAttribute("searchKeyword", null);
        model.addAttribute("sortField", "regDate");

        return "user/list";
    }


    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        // ItemVO itemDetail = dummyService.getShopItemDetail(itemId.longValue()); // ❌ DUMMY METHOD 주석 처리

        // model.addAttribute("isLoggedIn", true);
        // model.addAttribute("item", itemDetail);
        // return "user/detail";

        // ⭐ 더미 사용 부분 주석 처리 및 임시 처리
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("item", new ItemVO()); // 빈 ItemVO로 임시 대체
        return "user/detail";
    }

    /* ❌ 삭제: 장바구니 조회 로직은 CartController로 이동
    @GetMapping("/cart")
    public String getCart(Model model, HttpSession session, RedirectAttributes ra) {
        model.addAttribute("isLoggedIn", true);
        int userId = getCurrentUserId(session);
        List<CartVO> cartList = userService.getCartList(userId);

        if (cartList == null || cartList.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "🛒 장바구니에 담긴 상품이 없습니다. 상품 목록에서 상품을 담아주세요.");
            return "redirect:/user/list";
        }

        model.addAttribute("cartList", cartList);
        return "user/cart";
    }
    */

    /* ❌ 삭제: 장바구니 추가 로직은 CartController로 이동
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
    */

    /* ❌ 삭제: 장바구니 수량 변경 로직은 CartController로 이동
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
    */

    /* ❌ 삭제: 장바구니 항목 삭제 로직은 CartController로 이동
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
    */

    /* ⭐ 주석 처리: 주문 로직 분리
    @GetMapping("/orderList")
    public String getOrderList(Model model, @RequestParam(required = false) String successMessage, @RequestParam(required = false) String errorMessage, HttpSession session) {

        List<OrderVO> orderList = userService.getOrderList(SessionUtil.getCurrentUserId(session)); // ⭐ SessionUtil 사용

        model.addAttribute("orderList", orderList);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "user/orderList";
    }
    */

    /* ⭐ 주석 처리: 주문 로직 분리
    @GetMapping("/order")
    public String getOrderPage(
            @RequestParam(name = "selectedItems", required = false) List<Integer> selectedItems,
            @RequestParam Map<String, String> allParams,
            Model model,
            RedirectAttributes ra,
            HttpSession session) {

        int userId = SessionUtil.getCurrentUserId(session); // ⭐ SessionUtil 사용

        if (selectedItems == null || selectedItems.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "주문할 상품이 없습니다.");
            return "redirect:/cart"; // ⭐ 장바구니 컨트롤러 경로로 변경
        }

        // List<CartVO> cartList = userService.getCartList(userId); // ❌ userService 대신
        List<CartVO> cartList = cartService.getCartList(userId); // ✅ cartService 사용

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
            return "redirect:/cart"; // ⭐ 장바구니 컨트롤러 경로로 변경
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
    */


    // UserController.java (placeOrder 메서드만 수정)

    /* ⭐ 주석 처리: 주문 로직 분리
    @PostMapping("/placeOrder")
    public String placeOrder(
            @ModelAttribute OrderVO order,
            @RequestParam(name = "recipientName", required = false) String finalRecipientName,
            @RequestParam(name = "phone", required = false) String finalPhone,
            @RequestParam(name = "address", required = false) String finalAddress,
            @RequestParam(name = "addressName", required = false) String finalAddressName,
            @RequestParam Map<String, String> itemAmounts,
            RedirectAttributes ra,
            HttpSession session) {

        int userId = SessionUtil.getCurrentUserId(session); // ⭐ SessionUtil 사용
        order.setUserId(userId);

        order.setOrdRecipientName(finalRecipientName); // OrderVO의 수령인 필드에 설정
        order.setPhone(finalPhone);
        order.setAddress(finalAddress);
        order.setStatus("결제 완료");

        try {
            Long orderId = userService.placeOrder(order, itemAmounts);

            ra.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다! (주문 ID: " + orderId + ")");
            return "redirect:/user/orderList";

        } catch (IllegalStateException e) {
            // 비즈니스 로직 오류 (주소 누락, 재고 부족 등)
            ra.addFlashAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            // 주소 누락 오류가 발생하면, 다시 주문 페이지로 돌려보내는 것이 사용자 경험상 좋습니다.
            return "redirect:/user/order";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "시스템 오류로 주문에 실패했습니다.");
            e.printStackTrace();
            return "redirect:/user/orderList";
        }
    }
    */


    /* ⭐ 주석 처리: 주문 로직 분리
    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable("id") Long orderId, Model model, HttpSession session) {

        OrderVO orderData = userService.getOrderDetail(orderId, SessionUtil.getCurrentUserId(session)); // ⭐ SessionUtil 사용

        if (orderData == null) {
            return "redirect:/user/orderList";
        }

        model.addAttribute("order", orderData);
        List<ItemVO> orderItems = userService.getOrderItems(orderId);
        model.addAttribute("orderItems", orderItems);

        return "user/orderDetail";
    }
    */

    /* ⭐ 주석 처리: 주문 로직 분리
    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes ra, HttpSession session) {
        try {
            userService.cancelOrder(orderId, SessionUtil.getCurrentUserId(session)); // ⭐ SessionUtil 사용
            ra.addFlashAttribute("successMessage", orderId + "번 주문이 취소되었으며, 재고가 복구되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "주문 취소 중 시스템 오류가 발생했습니다.");
        }

        return "redirect:/user/orderList";
    }
    */

    /* ⭐ 주석 처리: 주소 관련 로직 분리
    @PostMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes ra, HttpSession session) {
        userService.deleteAddress(addressId, SessionUtil.getCurrentUserId(session)); // ⭐ SessionUtil 사용
        ra.addFlashAttribute("successMessage", addressId + "번 배송지가 삭제되었습니다.");
        return "redirect:/user/orderList";
    }
    */
}