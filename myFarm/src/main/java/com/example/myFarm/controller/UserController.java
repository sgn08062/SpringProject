package com.example.myFarm.controller;

import com.example.myFarm.command.ImageVO;
import com.example.myFarm.command.ShopVO;
import com.example.myFarm.command.UserVO;
import com.example.myFarm.image.ImageService;
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
    private final ImageService imageService;

    @GetMapping("/list")
    public Object productList(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
            @RequestParam(value = "isAjax", required = false, defaultValue = "false") boolean isAjax,
            Model model,
            HttpSession session
    ) {

        // 1. 사용자 인증 및 정보 조회
        boolean isLoggedIn = session.getAttribute(SessionUtil.USER_ID_SESSION_KEY) != null;
        int userId = isLoggedIn ? SessionUtil.getCurrentUserId(session) : 0;

        String userName = "게스트";
        if (isLoggedIn) {
            UserVO user = userService.getUserInfo(userId);
            if (user != null && user.getUserName() != null) {
                userName = user.getUserName();
            }
        }

        // 2. 상품 목록 조회 (검색 기능 사용)
        List<ShopVO> itemList = userService.getShopItemList(searchKeyword);

        for (ShopVO item : itemList) {
            // 해당 아이템의 이미지 목록을 DB에서 조회
            List<ImageVO> images = imageService.getImagesByItemId(item.getItemId());

            // 대표 이미지 (MAIN)의 URL만 추출합니다.
            String mainImageUrl = images.stream()
                    .filter(img -> "MAIN".equals(img.getImageType()))
                    .map(ImageVO::getImageUrl)
                    .findFirst()
                    .orElse("MAIN 이미지 없음");

            item.setImages(images);
        }

        // 3. Model 설정
        model.addAttribute("itemList", itemList);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("userName", userName);

        return "user/list";
    }


    // 2. AJAX 검색 요청 처리 (JSON 전담)
    @GetMapping("/list/search-ajax")
    @ResponseBody
    public List<ShopVO> productListAjax(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword
    ) {
        List<ShopVO> itemList = userService.getShopItemList(searchKeyword);

        // AJAX 응답(JSON)에도 이미지 정보 포함
        for (ShopVO item : itemList) {
            List<ImageVO> images = imageService.getImagesByItemId(item.getItemId());
            item.setImages(images); // ShopVO에 setImages(List<ImageVO> images)가 필요함

            // 대표 이미지 (MAIN)의 URL만 추출합니다.
            String mainImageUrl = images.stream()
                    .filter(img -> "MAIN".equals(img.getImageType()))
                    .map(ImageVO::getImageUrl)
                    .findFirst()
                    .orElse("MAIN 이미지 없음");
        }
        return itemList;
    }

    @GetMapping("/select-item-detail-json/{itemId}")
    @ResponseBody
    public ShopVO selectItemDetailJson(@PathVariable Long itemId) {
        ShopVO item = userService.selectItemDetail(itemId);
        List<ImageVO> images = imageService.getImagesByItemId(itemId);
        if (item != null) {
            item.setImages(images);
        }
        return item;
    }
}