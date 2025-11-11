# API Specification

> 2025년 11월 10일
> 수정 날짜 입력란
> (수정을 하게 된다면 "(갱신됨) 수정 날짜"를 표시하세요)

2025년 11월 10일 - 주소 관리 메서드에 주소 삭제 메서드 추가 <br>
2025년 11월 11일 - 어드민 관리 페이지 입장 url 및 controller 방식을 REST API 방식으로 수정한 api 추가

## COMMON API

### 🧑‍💻 계정 (/account)
🧭 페이지 (Controller, SSR)
| 행동          | API                | method | 비고            |
| :---------- | :----------------- | :----- | :------------ |
| 로그인 페이지     | /account/login     | GET    | 로그인 화면 반환     |
| 회원가입 페이지    | /account/register  | GET    | 회원가입 화면 반환    |

🔌 백엔드 API (RestController, JSON)
| 행동       | API                   | method | 비고                                                                                        |
| :------- | :-------------------- | :----- | :---------------------------------------------------------------------------------------- |
| 로그인      | /api/account/login    | POST   | 본문: x-www-form-urlencoded(loginId, userPw) / 응답: "success" 또는 "fail" / 성공 시 세션에 userId 저장 |
| 로그아웃     | /api/account/logout   | POST   | 세션 무효화 / 응답: "logout success" 또는 204                                                      |
| 회원가입 | /api/account/register | POST   | 본문: x-www-form-urlencoded(...) / 응답: "success" 또는 "fail"                                  |
| 중복 아이디 체크 | /api/account/idcheck | POST | 본문: x-www-form-urlencoded(loginId) / 응답: "dup" 또는 "unique" |


### 🛍️ 상품 목록 (/common)

| 행동 | API | method | 비고 |
| :--- | :--- | :--- | :--- |
| 상품 목록 조회 | /shop | GET | ex) /common/products |
| 상품 검색 | /shop/search | GET | |
| 상품 상세 페이지 | /shop/{id} | GET | |

---

## 🔒 관리자 API (/admin)
### 관리자 전용 페이지
| 행동 | API | method |
| :--- | :--- | :--- |
| 관리자 페이지 입장 | /admin | GET

### 🌿 농작물 관리 메서드 (/crops)

* **Base URL:** `/admin/crops`

| 행동 | API | method |
| :--- | :--- | :--- |
| 농작물 조회 | /api/crops | GET |
| 농작물 추가 html | /crops/addCrop | GET |
| 농작물 추가 | /crops/addCrop | POST |
| 농작물 삭제 | /crops/deleteCrop/{id} | DELETE |
| 농작물 활성화 | /crops/enable/{id} | POST |
| 농작물 비활성화 | /crops/disable/{id} | POST |

### 🛒 판매 사이트 상품 관리 메서드 (/shop)

* **Base URL:** `/admin/shop`

| 행동 | API | method |
| :--- | :--- | :--- |
| 상품 목록 조회 (관리자 전용) | /shop | GET |
| 상품 등록 html | /shop/additem | GET |
| 상품 등록 | /shop/additem | POST |
| 상품 수정 | /shop/item/{id} | PUT |
| 상품 삭제 | /shop/item/{id} | DELETE |

### 📋 판매 사이트 주문 관리 메서드 (/order)

| 행동 | API | method |
| :--- | :--- | :--- |
| 주문 목록 조회 | /order | GET |
| 주문 상세 조회 | /order/{id} | GET |
| 주문 상태 변경 | /order/status/{id} | PATCH |

### 📦 창고 관리 메서드 (/inventory)

| 행동 | API | method |
| :--- | :--- | :--- |
| 창고 조회 | /inventory | GET |

---

## 👤 소비자 API (/user)

### 🧺 장바구니 관리 메서드

| 행동 | API | method |
| :--- | :--- | :--- |
| 장바구니 목록 | /cart | GET |
| 장바구니에 아이템 추가 | /pushCart | POST |
| 장바구니에서 아이템 삭제 | /popCart | POST |

### 🧾 주문 관리 메서드

| 행동 | API | method |
| :--- | :--- | :--- |
| 주문 목록 조회 | /orders | GET |
| 주문 상세 조회 | /order/{id} | GET |
| 주문 취소 | /order/cancel/{id} | PATCH |

### 🏠 주소 관리 메서드

| 행동 | API | method |
| :--- | :--- | :--- |
| 주소 추가 | /address/insert/{id} | POST |
| 주소 수정 | /address/update/{id} | POST |
| 주소 삭제 | /address/delete/{id} | POST |
| 주소 조회 페이지 | /address | GET |
| 주소 추가 페이지 | /address/insert/{id} | GET |
| 주소 수정 페이지 | /address/update/{id} | GET |
