package com.example.myFarm.controller;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.ItemVO;
import com.example.myFarm.command.CartVO;
import com.example.myFarm.util.PageVO;
import com.example.myFarm.uorder.OrderService;
import com.example.myFarm.cart.CartService;
import com.example.myFarm.util.Criteria;
import com.example.myFarm.util.SessionUtil;
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
@RequestMapping("/uorder")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping("/orders")
    public String getOrderPage(@RequestParam List<Integer> selectedItems,
                               @RequestParam Map<String, String> itemAmounts,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra) {
        int userId = SessionUtil.getCurrentUserId(session);

        if (selectedItems == null || selectedItems.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "주문할 상품이 없습니다.");
            return "redirect:/cart";
        }

        try {
            List<CartVO> cartList = cartService.getCartList(userId);

            List<ItemVO> checkoutItems = cartList.stream()
                    .filter(cart -> selectedItems.contains(cart.getItemId()))
                    .map(cart -> {
                        String amountKey = "itemAmount_" + cart.getItemId();
                        int finalAmount = cart.getAmount();

                        if (itemAmounts.containsKey(amountKey)) {
                            try {
                                finalAmount = Math.max(1, Integer.parseInt(itemAmounts.get(amountKey)));
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
                    .filter(item -> item.getStockAmount() >= item.getOrderAmount())
                    .collect(Collectors.toList());

            if (checkoutItems.isEmpty()) {
                ra.addFlashAttribute("errorMessage", "선택하신 상품을 찾을 수 없거나 이미 품절된 상품이 포함되어 주문할 수 없습니다.");
                return "redirect:/cart";
            }

            model.addAttribute("checkoutItems", checkoutItems);

            AddressVO defaultAddress = orderService.getDefaultAddress(userId);
            List<AddressVO> otherAddresses = orderService.getOtherAddresses(userId);

            model.addAttribute("defaultAddress", defaultAddress);
            model.addAttribute("otherAddresses", otherAddresses);

            int totalPrice = checkoutItems.stream()
                    .mapToInt(item -> item.getPrice() * item.getOrderAmount())
                    .sum();
            model.addAttribute("totalPrice", totalPrice);

            model.addAttribute("userId", userId);

            return "uorder/orders";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/place")
    public String placeOrder(@ModelAttribute OrderVO order,
                             @RequestParam Map<String, String> itemAmounts,
                             HttpSession session,
                             RedirectAttributes ra) {

        int userId = SessionUtil.getCurrentUserId(session);
        order.setUserId(userId);

        try {
            Long orderId = orderService.placeOrder(order, itemAmounts);

            ra.addFlashAttribute("message", orderId + "번 주문이 완료되었습니다.");

            // 🚨 수정된 부분: 상세 페이지 -> 목록 페이지로 변경
            return "redirect:/uorder/list";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/uorder/orders";
        }
    }

    @GetMapping("/list")
    public String getOrderList(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "1", name = "page") int pageNum, // 현재 페이지 번호 (page)
            @RequestParam(defaultValue = "10", name = "size") int amount, // 페이지당 항목 수 (size)
            @RequestParam(required = false) String startDate, // 조회 시작일
            @RequestParam(required = false) String endDate // 조회 종료일
    ) {

        int userId = SessionUtil.getCurrentUserId(session);

        // 🌟 [추가된 부분] userName을 조회하여 Model에 담기
        String userName = orderService.getUserName(userId);
        model.addAttribute("userName", userName);
        // ------------------------------------

        // 1. Criteria 객체 생성 및 검색 파라미터 설정
        Criteria cri = new Criteria(pageNum, amount);
        cri.setStartDate(startDate);
        cri.setEndDate(endDate);

        // 2. Service에서 페이징 정보를 포함한 Map을 받음
        Map<String, Object> resultMap = orderService.getOrderListWithPaging(userId, cri);

        // 3. 모델에 데이터 추가 (View에서 ${userName} 사용 가능)
        model.addAttribute("orderList", resultMap.get("orderList"));
        model.addAttribute("pageVO", resultMap.get("pageVO"));

        // 4. 검색 폼 유지를 위해 검색 조건도 모델에 추가
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "uorder/orderList";
    }

    @GetMapping("/detail/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        OrderVO order = orderService.getOrderDetail(orderId, userId);

        List<ItemVO> items = orderService.getOrderItems(orderId);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "uorder/orderDetail";
    }

    @PostMapping("/cancel/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, HttpSession session, RedirectAttributes ra) {
        int userId = SessionUtil.getCurrentUserId(session);
        try {
            orderService.cancelOrder(orderId, userId);
            ra.addFlashAttribute("message", orderId + "번 주문이 취소되었습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/uorder/list";
    }

    @GetMapping("/address")
    public String getAddressList(HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        List<AddressVO> list = orderService.getAddressList(userId);

        List<AddressVO> otherList = orderService.getOtherAddresses(userId);

        model.addAttribute("addressList", list);
        model.addAttribute("otherAddressList", otherList);
        return "uorder/addressList";
    }

    @GetMapping("/address/detail")
    public String getAddressDetail(@RequestParam long addressId, HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        AddressVO address = orderService.getAddressDetail(addressId, userId);
        model.addAttribute("address", address);
        return "uorder/addressDetail";
    }

    @PostMapping("/address/save")
    public String saveAddress(@ModelAttribute AddressVO addressForm, HttpSession session) {
        int userId = SessionUtil.getCurrentUserId(session);
        addressForm.setUserId(userId);
        orderService.saveAddress(addressForm);
        return "redirect:/uorder/address";
    }

    /*
    @PostMapping("/address/delete")
    public String deleteAddress(@RequestParam long addressId, HttpSession session) {
        int userId = SessionUtil.getCurrentUserId(session);
        orderService.deleteAddress(addressId, userId);
        return "redirect:/uorder/address";
    }

    @GetMapping("/info")
    public String getUserInfo(HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        String userName = orderService.getUserName(userId);
        model.addAttribute("userName", userName);
        return "uorder/info";
    }
     */
}