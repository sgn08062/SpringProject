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
        int userId = SessionUtil.getCurrentUserId(session);
        boolean isLoggedIn = userId != 0;

        String userName = "게스트";
        if (isLoggedIn) {
            UserVO user = userService.getUserInfo(userId);
            if (user != null && user.getUserName() != null) {
                userName = user.getUserName();
            }
        }

        // 2. 상품 목록 조회 (검색 기능 사용)
        List<ShopVO> itemList = userService.getShopItemList(searchKeyword);

        // ⭐️⭐️ [DEBUG LOG START] (최초 로드 시 DB URL 확인) ⭐️⭐️
        System.out.println("--- [DEBUG] UserController: 상품 목록 처리 시작 (DB URL 확인) ---");

        // 3. 상품 목록에 **대표 이미지** 정보 추가
        for (ShopVO item : itemList) {
            // 해당 아이템의 이미지 목록을 DB에서 조회
            List<ImageVO> images = imageService.getImagesByItemId(item.getItemId());

            // 대표 이미지 (MAIN)의 URL만 추출합니다.
            String mainImageUrl = images.stream()
                    .filter(img -> "MAIN".equals(img.getImageType()))
                    .map(ImageVO::getImageUrl)
                    .findFirst()
                    .orElse("MAIN 이미지 없음");

            // ⭐️ 디버그 로그: 상품 ID와 DB에서 추출된 MAIN 이미지 URL 출력
            System.out.println(" - ItemId: " + item.getItemId() +
                    ", ItemName: " + item.getItemName() +
                    ", MainImageUrl (DB 경로): " + mainImageUrl);

            // View에서 사용 가능하도록 ShopVO에 List<ImageVO> 전체를 설정
            item.setImages(images);
        }

        System.out.println("----------------------------------------------------------");
        // ⭐️⭐️ [DEBUG LOG END] ⭐️⭐️

        // 4. Model 설정
        model.addAttribute("itemList", itemList);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("userName", userName);

        return "user/list";
    }

    // --- (구분선) ---

    // 2. AJAX 검색 요청 처리 (JSON 전담)
    @GetMapping("/list/search-ajax")
    @ResponseBody
    public List<ShopVO> productListAjax(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword
    ) {
        List<ShopVO> itemList = userService.getShopItemList(searchKeyword);

        // ⭐️⭐️ [DEBUG LOG START] (AJAX 요청 시 DB URL 확인) ⭐️⭐️
        System.out.println("--- [DEBUG] UserController: AJAX 검색 결과 처리 시작 (DB URL 확인) ---");

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

            // ⭐️ 디버그 로그: 상품 ID와 DB에서 추출된 MAIN 이미지 URL 출력
            System.out.println(" - AJAX ItemId: " + item.getItemId() +
                    ", ItemName: " + item.getItemName() +
                    ", MainImageUrl (DB 경로): " + mainImageUrl);
        }

        System.out.println("----------------------------------------------------------");
        // ⭐️⭐️ [DEBUG LOG END] ⭐️⭐️

        return itemList;
    }


    @GetMapping("/detail")
    public String productDetail(@RequestParam Integer itemId, Model model) {
        // itemId를 사용하여 상세 정보를 조회
        ShopVO item = userService.getItemDetail(itemId.longValue());

        // 상세 페이지를 위한 모든 이미지 정보 추가
        List<ImageVO> images = imageService.getImagesByItemId(item.getItemId());
        item.setImages(images); // ShopVO에 setImages(List<ImageVO> images)가 필요함

        model.addAttribute("isLoggedIn", true); // 세션 확인 로직 필요
        model.addAttribute("item", item);
        return "user/detail";
    }
}