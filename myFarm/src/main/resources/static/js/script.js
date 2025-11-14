// --- 맞춤형 알림창 ---
function showCustomAlert(message, type = 'info') {
    const existingAlert = document.getElementById('custom-alert');
    if (existingAlert) existingAlert.remove();
    const alertBox = document.createElement('div');
    alertBox.id = 'custom-alert';
    alertBox.innerHTML = `
      <span style="font-size: 1.2rem; margin-right: 10px;">
        ${type === 'success' ? '🪴' : (type === 'error' ? '🧺' : 'ℹ️')}
      </span>
      ${message} 
    `;
    alertBox.style.cssText = `
      position: fixed; top: 80px; left: 50%; transform: translateX(-50%);
      background-color: ${type === 'success' ? 'var(--brand-primary)' : (type === 'error' ? '#FFF0F1' : 'var(--brand-surface)')};
      color: ${type === 'success' ? '#fff' : 'var(--text-primary)'};
      border: 1px solid ${type === 'success' ? 'var(--brand-primary)' : 'var(--brand-border)'};
      padding: 15px 25px; border-radius: 30px;
      box-shadow: 0 4px 10px rgba(0,0,0,0.1); z-index: 1001;
      font-weight: 600; opacity: 0;
      transition: opacity 0.3s ease, top 0.3s ease;
    `;
    document.body.appendChild(alertBox);
    setTimeout(() => {
        alertBox.style.opacity = '1';
        alertBox.style.top = '90px';
    }, 10);
    setTimeout(() => {
        alertBox.style.opacity = '0';
        alertBox.style.top = '80px';
        setTimeout(() => { if (alertBox.parentElement) alertBox.remove(); }, 300);
    }, 2500);
}

// --- 전역 UI 요소 ---
const checkoutSection = document.querySelector('.checkout');
const cartItemsList = document.querySelector('.cart-items-list');
const cartBadge = document.querySelector('.cart-badge');
const cartToggleBtn = document.getElementById('cart-toggle-btn');

// 로그인/비로그인 UI
const beforeLoginNav = document.getElementById('before-login');
const afterLoginNav = document.getElementById('after-login');
const userGreetingSpan = document.getElementById('user-greeting');

// 모달
const loginModal = document.getElementById('login-modal-overlay');
// [삭제] 회원가입 모달 변수 제거
// const signupModal = document.getElementById('signup-modal-overlay');

// [v10] 전역 변수: 현재 장바구니 상태 저장 (주문하기 페이지 이동 시 사용)
let currentCartItems = [];


// --- [v12] 장바구니 로직 (최종 API 연동) ---

/**
 * [v12] 장바구니 화면을 렌더링합니다.
 * (CartViewDTO의 필드명에 맞게 수정)
 */
function renderCart(items) {
    cartItemsList.innerHTML = '';
    let total = 0;
    let totalQty = 0;
    currentCartItems = items; // 주문하기용 전역 변수 업데이트

    if (!items || items.length === 0) {
        cartItemsList.innerHTML = '<p style="text-align: center; color: var(--text-secondary); margin-top: 20px;">장바구니가 비어있습니다.</p>';
    } else {
        items.forEach(item => {
            // [v12 수정] CartViewDTO의 필드명 사용: itemName, amount, price
            const itemTotalPrice = item.price * item.amount;
            total += itemTotalPrice;
            totalQty += item.amount;

            const div = document.createElement('div');
            div.classList.add('cart-item');
            div.innerHTML = `
        <span class="item-name">${item.itemName}</span>
        <div class="item-controls">
          <button class="btn-decrease" data-item-id="${item.itemId}">-</button>
          <span>${item.amount}</span>
          <button class="btn-increase" data-item-id="${item.itemId}">+</button>
        </div>
        <span class="item-price">₩${itemTotalPrice.toLocaleString()}</span>
      `;
            cartItemsList.appendChild(div);
        });
    }

    checkoutSection.querySelector('p').innerHTML = `<strong>총합계: ₩${total.toLocaleString()}</strong>`;

    if (totalQty > 0) {
        cartBadge.textContent = totalQty;
        cartBadge.style.display = 'inline-block';
    } else {
        cartBadge.style.display = 'none';
    }
}

/**
 * [v12] (수정) 서버에서 현재 장바구니 목록을 불러옵니다.
 */
async function loadCart() {
    try {
        const response = await fetch('/api/cart');
        if (!response.ok) {
            if (response.status === 401) {
                renderCart([]);
                throw new Error('401 Unauthorized');
            }
            throw new Error('장바구니 정보를 불러오는 데 실패했습니다.');
        }
        const cartItems = await response.json();
        renderCart(cartItems);
    } catch (error) {
        if (error.message.includes('401')) throw error;
        console.error(error);
        showCustomAlert('장바구니 로딩 중 오류 발생', 'error');
    }
}

/**
 * [v12] (수정) 장바구니에 아이템을 '추가/수정'합니다.
 * (quantity가 음수이면 수량 감소 처리)
 */
async function addToCart(itemId, quantity) {
    try {
        const response = await fetch('/api/cart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            // 백엔드는 productId를 itemId로 간주
            body: JSON.stringify({ productId: itemId, quantity: quantity })
        });

        if (response.ok) {
            await loadCart();
        } else if (response.status === 401) {
            showCustomAlert('로그인이 필요합니다.', 'error');
            openModal(loginModal);
        } else {
            const errorText = await response.text();
            showCustomAlert(`장바구니 업데이트 실패: ${errorText}`, 'error');
        }
    } catch (error) {
        console.error(error);
        showCustomAlert('장바구니 업데이트 중 오류 발생', 'error');
    }
}

// (상품 목록의 '장바구니 담기' 버튼)
document.querySelectorAll('.add-btn').forEach(button => {
    button.addEventListener('click', e => {
        // data-product-id는 이제 data-item-id로 사용됩니다.
        const itemId = e.target.dataset.productId;
        if (!itemId) {
            console.error('HTML에 data-product-id 속성이 없습니다!');
            showCustomAlert('상품 ID가 없어 추가할 수 없습니다.', 'error');
            return;
        }
        addToCart(Number(itemId), 1);
    });
});

// [v12 수정] 장바구니 내 +/- 버튼 이벤트
cartItemsList.addEventListener('click', e => {
    const target = e.target;

    if (target.classList.contains('btn-increase')) {
        // [v12 수정] data-item-id 사용
        const itemId = target.dataset.itemId;
        addToCart(Number(itemId), 1);
    }

    if (target.classList.contains('btn-decrease')) {
        // [v12 수정] data-item-id 사용 및 quantity: -1 전송
        const itemId = target.dataset.itemId;
        addToCart(Number(itemId), -1);
    }
});

// ('주문하기' 버튼 - v10과 동일)
document.getElementById('start-order-btn').addEventListener('click', () => {
    if (!currentCartItems || currentCartItems.length === 0) {
        showCustomAlert('장바구니가 비어 있습니다.', 'error');
        return;
    }

    localStorage.setItem('orderCart', JSON.stringify(currentCartItems));
    window.location.href = '/checkout';
});

// (장바구니 토글 버튼 - v7과 동일)
cartToggleBtn.addEventListener('click', (e) => {
    e.preventDefault();
    document.body.classList.toggle('cart-hidden');
    // handleCartFooterCollision(); // (v3~v7) 관련 함수가 없으므로 주석 처리
});


// --- [v10] 인증 로직 (API 연동) ---

/**
 * [v10 신규] 로그인/비로그인 상태에 따라 UI를 변경합니다.
 */
function updateLoginUI(isLoggedIn, user = null) {
    if (isLoggedIn) {
        // [v12 수정] user.userName 사용 (MemberDTO 필드명)
        userGreetingSpan.textContent = `${user.userName}님`;
        beforeLoginNav.classList.add('hidden');
        afterLoginNav.classList.remove('hidden');
        // [v12 수정] 로그인 시 currentUser 저장 (checkout.js용)
        localStorage.setItem('currentUser', JSON.stringify(user));
    } else {
        userGreetingSpan.textContent = '';
        beforeLoginNav.classList.remove('hidden');
        afterLoginNav.classList.add('hidden');
        // [v12 수정] 로그아웃 시 currentUser 삭제
        localStorage.removeItem('currentUser');
    }
}

/**
 * [v10 신규] 페이지 로드 시, 로그인 상태를 서버에 확인합니다.
 */
async function checkLoginStatus() {
    try {
        const response = await fetch('/api/member/me');

        if (response.ok) {
            const user = await response.json();
            updateLoginUI(true, user);
            await loadCart();
        } else {
            updateLoginUI(false);
            renderCart([]);
        }
    } catch (error) {
        console.error('로그인 상태 확인 중 오류:', error);
        updateLoginUI(false);
        renderCart([]);
    }
}

// (모달 열기/닫기)
const authLink = document.getElementById('auth-link');
const switchToSignupBtn = document.getElementById('modal-switch-to-signup');
// [삭제] 회원가입 -> 로그인 전환 버튼 제거
// const switchToLoginBtn = document.getElementById('modal-switch-to-login');
const closeButtons = document.querySelectorAll('.modal-close-btn');

function openModal(modal) { if(modal) modal.classList.add('visible'); }
function closeModal(modal) { if(modal) modal.classList.remove('visible'); }

authLink.addEventListener('click', (e) => { e.preventDefault(); openModal(loginModal); });

// [수정] 회원가입 버튼 클릭 시, 다른 분들이 만든 회원가입 페이지로 이동하도록 처리
// (예: /signup 페이지로 이동)
if (switchToSignupBtn) {
    switchToSignupBtn.addEventListener('click', () => {
        // closeModal(loginModal);
        // openModal(signupModal); // [삭제] 모달 여는 대신 페이지 이동
        window.location.href = '/signup'; // 실제 회원가입 페이지 경로로 수정 필요
    });
}

// [삭제] 로그인 -> 회원가입 전환 버튼 이벤트 제거
// switchToLoginBtn.addEventListener('click', () => { closeModal(signupModal); openModal(loginModal); });

closeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const targetModal = document.getElementById(btn.dataset.closeTarget);
        closeModal(targetModal);
    });
});



// --- [v11] 로그인/회원가입 '버튼' 이벤트 (API 연동) ---

// (로그인 버튼 클릭)
document.getElementById('login-submit-btn').addEventListener('click', async () => {
    const id = document.getElementById('login-id').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch('/api/member/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: id, password: password })
        });

        if (response.ok) {
            closeModal(loginModal);
            showCustomAlert('로그인되었습니다.', 'success');
            await checkLoginStatus();
        } else {
            const errorText = await response.text();
            showCustomAlert(errorText, 'error');
        }
    } catch (error) {
        console.error(error);
        showCustomAlert('로그인 중 오류가 발생했습니다.', 'error');
    }
});

// (로그아웃 버튼 클릭)
document.getElementById('logout-btn').addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        const response = await fetch('/api/member/logout', { method: 'POST' });
        if (response.ok) {
            showCustomAlert('로그아웃되었습니다.', 'info');
            updateLoginUI(false);
            renderCart([]);
        }
    } catch (error) {
        console.error(error);
        showCustomAlert('로그아웃 중 오류가 발생했습니다.', 'error');
    }
});


// (회원가입 버튼 클릭)
// [삭제] 회원가입 관련 로직 (변수 선언, 이벤트 리스너, API 호출) 모두 제거

// (아이디 중복확인)
// [삭제] 아이디 중복확인 관련 로직 모두 제거

// (기타 유효성 검사 UI 생략)
// [삭제] 회원가입 유효성 검사 로직 모두 제거

// (주소 검색 시뮬레이션)
// [삭제] 회원가입 주소 검색 로직 모두 제거


// --- [v3~v7] 기타 로직 (검색, 상세, 푸터 충돌) ---

// (검색, 카테고리, 상세 뷰 로직 등은 v10과 동일하며, itemId 사용에 맞춰 수정되어 있습니다.)
// ... (생략) ...

// --- [v10] 페이지 최초 로드 ---
document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
});

// --- [v13 신규] 카테고리 필터링 로직 ---

// 상품 카드 전체를 가져옵니다.
const productCards = document.querySelectorAll('.product-card');

// --- [v13 신규] 검색 필터링 로직 ---

const searchInput = document.querySelector('.search-bar input[type="text"]');
const searchResultsMsg = document.getElementById('search-results-msg');
const searchMsgSpan = searchResultsMsg.querySelector('span');

/**
 * 검색어에 따라 상품 목록을 필터링합니다.
 * @param {string} searchTerm - 검색어
 */
function filterProductsBySearch(searchTerm) {
    const term = searchTerm.toLowerCase().trim();
    let resultsFound = 0;

    // 상품 카드는 이미 전역 변수 productCards로 선언되어 있다고 가정합니다.
    // const productCards = document.querySelectorAll('.product-card');

    productCards.forEach(card => {
        // 상품 제목(h3)을 기준으로 검색합니다.
        const productName = card.querySelector('h3').textContent.toLowerCase();

        if (productName.includes(term)) {
            card.style.display = 'block';
            resultsFound++;
        } else {
            card.style.display = 'none';
        }
    });

    // 검색 결과 메시지 업데이트
    if (term.length > 0 && resultsFound === 0) {
        searchMsgSpan.textContent = term;
        searchResultsMsg.style.display = 'block';
    } else {
        searchResultsMsg.style.display = 'none';
    }
}

// 1. 검색창에 'input' 이벤트 리스너 추가 (글자가 입력될 때마다 필터링)
searchInput.addEventListener('input', (e) => {
    // 검색창에 입력이 시작되면, 현재 활성화된 카테고리 표시를 '전체보기'로 재설정합니다.
    categoryLinks.forEach(item => item.classList.remove('active'));
    document.querySelector('.category-sidebar ul li a[data-category="all"]').classList.add('active');

    // 검색 실행
    filterProductsBySearch(e.target.value);
});


/**
 * 선택된 카테고리에 따라 상품 목록을 필터링하고 UI를 업데이트합니다.
 * @param {string} categoryKey - 'all', 'fruit', 'veg', 'etc' 중 하나
 */
function filterProducts(categoryKey) {
    // 1. 상품 카드 필터링
    productCards.forEach(card => {
        const productCategory = card.getAttribute('data-category');

        // 'all'이거나, 상품의 카테고리가 선택된 카테고리와 일치하면 보이게 합니다.
        if (categoryKey === 'all' || productCategory === categoryKey) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// 카테고리 링크 전체를 가져옵니다.
const categoryLinks = document.querySelectorAll('.category-sidebar ul li a');

// 2. 카테고리 링크에 클릭 이벤트 리스너 추가
categoryLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault(); // 링크의 기본 동작 (페이지 이동) 방지

        // 2-1. UI 활성화/비활성화
        categoryLinks.forEach(item => item.classList.remove('active')); // 모든 링크의 active 클래스 제거
        e.target.classList.add('active'); // 클릭된 링크에 active 클래스 추가

        // 2-2. 카테고리 필터링 실행
        const selectedCategory = e.target.getAttribute('data-category');
        if (selectedCategory) {
            filterProducts(selectedCategory);
        }
    });
});

// 페이지 로드 시 'all' 카테고리로 초기 필터링 (선택 사항)
document.addEventListener('DOMContentLoaded', () => {
    // ... 기존 checkLoginStatus() 호출 등 ...

    // 카테고리 초기 설정
    filterProducts('all');
});