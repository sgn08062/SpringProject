package com.example.myFarm.controller;

import com.example.myFarm.command.CartVO;
import com.example.myFarm.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    public record OrderItemVO(
            String itemName,
            int price,
            int amount
    ) {}

    public record OrderVO(
            Long orderId,
            String status,
            int totalAmount,
            LocalDate orderDate,
            String representativeItemName
    ) {}

    public record AddressVO(
            Long addId,
            String addName,
            String address
    ) {}

    public record Item(Integer itemId, String itemName, int price, int amount) {
        public String unitName() {
            return "개";
        }
    }

    private int getCurrentUserId() {
        return 1;
    }

    private Item getItemFromDB(Integer itemId) {
        return switch (itemId) {
            case 1 -> new Item(1, "유기농 토마토", 5000, 50);
            case 2 -> new Item(2, "친환경 상추", 3500, 0);
            case 3 -> new Item(3, "무농약 당근", 2000, 100);
            case 4 -> new Item(4, "꿀 사과", 7000, 80);
            case 5 -> new Item(5, "제주 감귤", 4500, 120);
            default -> new Item(itemId, "샘플 상품 " + itemId, 6000, 50);
        };
    }

    private List<Item> getAllItemsFromDB() {
        return List.of(
                getItemFromDB(1),
                getItemFromDB(2),
                getItemFromDB(3),
                getItemFromDB(4),
                getItemFromDB(5)
        );
    }

    private List<OrderVO> getOrderListFromDB() {
        return List.of(
                new OrderVO(1L, "배송 중", 25000, LocalDate.now().minusDays(5), "유기농 감자 외 2개"),
                new OrderVO(2L, "주문 대기", 14500, LocalDate.now().minusDays(1), "친환경 당근"),
                new OrderVO(3L, "배송 완료", 30000, LocalDate.now().minusDays(10), "특품 멜론")
        );
    }

    private List<AddressVO> getAddressListFromDB() {
        return List.of(
                new AddressVO(10L, "집", "서울시 강남구 테헤란로 123"),
                new AddressVO(11L, "회사", "경기도 성남시 분당구 판교로 456")
        );
    }

    @GetMapping("/list")
    public String productList(Model model) {
        List<Item> itemList = getAllItemsFromDB();
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("itemList", itemList);
        return "user/list";
    }

    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        Item itemDetail = getItemFromDB(itemId);

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
        int userId = getCurrentUserId();
        try {
            userService.deleteCartItem(userId, itemId);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", "장바구니 항목 삭제 실패: " + e.getMessage());
        }
    }

    @GetMapping("/orders")
    public String getOrderList(Model model, @RequestParam(required = false) String successMessage) {
        List<OrderVO> orderList = getOrderListFromDB();
        List<AddressVO> addressList = getAddressListFromDB();

        model.addAttribute("orderList", orderList);
        model.addAttribute("addressList", addressList);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        return "user/manage";
    }

    @GetMapping("/order/{id}")
    public String getOrderDetail(@PathVariable("id") Long orderId, Model model) {
        OrderVO orderData = getOrderListFromDB().stream()
                .filter(order -> order.orderId().equals(orderId))
                .findFirst()
                .orElse(null);

        if (orderData == null) {
            return "redirect:/user/orders";
        }

        model.addAttribute("order", orderData);
        model.addAttribute("orderItems", List.of(
                new OrderItemVO("샘플 상품 A", 5000, 2),
                new OrderItemVO("샘플 상품 B", 10000, 1)
        ));

        return "user/orderDetail";
    }

    @PostMapping("/order/cancel/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, RedirectAttributes ra) {
        ra.addFlashAttribute("successMessage", orderId + "번 주문이 취소되었습니다.");
        return "redirect:/user/orders?tab=orderListTab";
    }

    @PostMapping("/address")
    public String saveAddress(@ModelAttribute AddressVO addressForm, RedirectAttributes ra) {
        String message;
        if (addressForm.addId() == null || addressForm.addId() == 0) {
            message = addressForm.addName() + " 배송지가 새로 등록되었습니다.";
        } else {
            message = addressForm.addName() + " 배송지 정보가 수정되었습니다.";
        }

        ra.addFlashAttribute("successMessage", message);
        return "redirect:/user/orders?tab=addressManageTab";
    }

    @PostMapping("/address/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long addressId, RedirectAttributes ra) {
        ra.addFlashAttribute("successMessage", addressId + "번 배송지가 삭제되었습니다.");
        return "redirect:/user/orders?tab=addressManageTab";
    }

    @GetMapping("/checkout")
    public String getCheckoutPage(Model model) {
        model.addAttribute("checkoutItems", List.of(
                new OrderItemVO("유기농 감자", 5000, 2),
                new OrderItemVO("친환경 당근", 3500, 3)
        ));
        model.addAttribute("totalPrice", 5000 * 2 + 3500 * 3);

        record TempAddressVO(Long addId, String addName, String address, String phone) {}

        model.addAttribute("defaultAddress", new TempAddressVO(1L, "기본", "서울시 강남구", "010-1234-5678"));
        return "user/order";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(RedirectAttributes ra) {
        ra.addFlashAttribute("successMessage", "주문이 성공적으로 완료되었습니다.");
        return "redirect:/user/orders?tab=orderListTab";
    }

    @GetMapping("/orderlist")
    public String getOrderListRedirect() {
        return "redirect:/user/orders";
    }
}