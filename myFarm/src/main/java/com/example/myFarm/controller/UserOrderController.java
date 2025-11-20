/*
package com.example.myFarm.controller;

import com.example.myFarm.command.AddressVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.command.ItemVO;
/import com.example.myFarm.uorder.OrderService;
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
@RequestMapping("/uorder")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public String getOrderPage(HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        model.addAttribute("userId", userId);
        return "uorder/orders";
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
            return "redirect:/uorder/detail/" + orderId;

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/list")
    public String getOrderList(HttpSession session, Model model) {
        int userId = SessionUtil.getCurrentUserId(session);
        List<OrderVO> orderList = orderService.getOrderList(userId);
        model.addAttribute("orderList", orderList);
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
        return "redirect:/uorder/detail/" + orderId;
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
}*/
