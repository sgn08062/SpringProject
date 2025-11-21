package com.example.myFarm.controller;

import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.UserVO;
import com.example.myFarm.user.UserService;
import com.example.myFarm.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    // AdminShopService는 상품 목록 조회에 사용되지 않으므로 제거하거나 주석 처리
    // private final AdminShopService adminShopService;

    @GetMapping("/list")
    public Object productList(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
            @RequestParam(value = "isAjax", required = false, defaultValue = "false") boolean isAjax,
            Model model,
            HttpSession session
    ) {
        // 1. 사용자 인증 및 정보 조회 (Header 처리를 위해 필요)
        int userId = SessionUtil.getCurrentUserId(session);
        boolean isLoggedIn = userId != 0; // userId 0은 보통 미로그인 상태를 가정

        String userName = "게스트";
        if (isLoggedIn) {
            UserVO user = userService.getUserInfo(userId);
            if (user != null && user.getUserName() != null) {
                userName = user.getUserName();
            }
        }

        // 2. 상품 목록 조회 (검색 기능 사용)
        // UserService의 getShopItemList 메서드는 정렬(sortField) 파라미터가 제거되었으므로,
        // searchKeyword만 넘겨주거나, 혹은 현재 UserService 정의에 맞춰 두 파라미터를 모두 넘깁니다.
        // 현재 UserService 정의: List<ShopVO> getShopItemList(String searchKeyword, String sortField);
        // UserServiceImpl의 실제 구현: return userMapper.selectShopItemList(searchKeyword);

        // 🚨 프론트엔드가 sortField를 보내고 있으므로, 임시로 sortField도 받지만, Service에서 무시한다고 가정하고 searchKeyword만 사용하도록 수정합니다.
        List<ShopVO> itemList = userService.getShopItemList(searchKeyword);

        model.addAttribute("itemList", itemList);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("userName", userName);

        return "user/list";
    }

    // 2. AJAX 검색 요청 처리 (JSON 전담)
    @GetMapping("/list/search-ajax")
    @ResponseBody // ⭐️ JSON 응답을 보장
    public List<ShopVO> productListAjax(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword
    ) {
        // 검색 키워드만 사용하여 데이터 조회
        return userService.getShopItemList(searchKeyword);
    }


    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        // itemId를 사용하여 상세 정보를 조회하고 모델에 추가하는 로직이 필요
        ShopVO item = userService.getItemDetail(itemId.longValue());

        model.addAttribute("isLoggedIn", true); // 세션 확인 로직 필요
        model.addAttribute("item", item);
        return "user/detail";
    }
}