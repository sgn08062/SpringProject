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
const signupModal = document.getElementById('signup-modal-overlay');

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

// [v12 삭제] deleteFromCart 함수는 더 이상 사용하지 않습니다.

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
    handleCartFooterCollision();
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

// (모달 열기/닫기 - v3와 동일)
const authLink = document.getElementById('auth-link');
const switchToSignupBtn = document.getElementById('modal-switch-to-signup');
const switchToLoginBtn = document.getElementById('modal-switch-to-login');
const closeButtons = document.querySelectorAll('.modal-close-btn');
function openModal(modal) { if(modal) modal.classList.add('visible'); }
function closeModal(modal) { if(modal) modal.classList.remove('visible'); }
authLink.addEventListener('click', (e) => { e.preventDefault(); openModal(loginModal); });
switchToSignupBtn.addEventListener('click', () => { closeModal(loginModal); openModal(signupModal); });
switchToLoginBtn.addEventListener('click', () => { closeModal(signupModal); openModal(loginModal); });
closeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const targetModal = document.getElementById(btn.dataset.closeTarget);
        closeModal(targetModal);
    });
});
[loginModal, signupModal].forEach(modal => {
    modal.addEventListener('click', (e) => { if (e.target === modal) closeModal(modal); });
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
const signupSubmitBtn = document.getElementById('signup-submit-btn');
const signupIdInput = document.getElementById('signup-id');
const signupPasswordInput = document.getElementById('signup-password');
const signupPasswordConfirmInput = document.getElementById('signup-password-confirm');
const signupNameInput = document.getElementById('signup-name');
const signupEmailIdInput = document.getElementById('signup-email-id');
const signupEmailDomainInput = document.getElementById('signup-email-domain');
const signupPhoneInput = document.getElementById('signup-phone');
const signupAddressInput = document.getElementById('signup-address');
const signupAddressDetailInput = document.getElementById('signup-address-detail');

signupSubmitBtn.addEventListener('click', async () => {
    const password = signupPasswordInput.value;
    const confirmPass = signupPasswordConfirmInput.value;

    if (!idCheckStatus) {
        showCustomAlert('아이디 중복확인을 해주세요.', 'error'); return;
    }
    if (password !== confirmPass) {
        showCustomAlert('비밀번호가 일치하지 않습니다.', 'error'); return;
    }

    const signupData = {
        username: signupIdInput.value,
        password: password,
        name: signupNameInput.value,
        phone: signupPhoneInput.value,
        // [v12 수정] address는 Address 테이블에 저장되지만, 요청은 script.js에서 보낸 그대로 전송
        address: `${signupAddressInput.value} ${signupAddressDetailInput.value}`,
    };

    try {
        const response = await fetch('/api/member/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(signupData)
        });

        if (response.ok) {
            showCustomAlert('회원가입 성공! 로그인 해주세요.', 'success');
            // (폼 비우기)
            signupIdInput.value = ''; signupPasswordInput.value = '';
            signupPasswordConfirmInput.value = ''; signupNameInput.value = '';
            signupEmailIdInput.value = ''; signupEmailDomainInput.value = '';
            signupPhoneInput.value = ''; signupAddressInput.value = '';
            signupAddressDetailInput.value = '';
            idCheckStatus = false;

            closeModal(signupModal);
            openModal(loginModal);
        } else {
            const errorText = await response.text();
            showCustomAlert(errorText, 'error');
        }
    } catch (error) {
        console.error(error);
        showCustomAlert('회원가입 중 오류가 발생했습니다.', 'error');
    }
});

// (아이디 중복확인 - v11과 동일)
let idCheckStatus = false;
const idCheckBtn = document.getElementById('id-check-btn');
const idMsg = document.getElementById('id-msg');

idCheckBtn.addEventListener('click', async () => {
    const id = signupIdInput.value;
    if (id.length < 4 || id.length > 20) {
        idMsg.textContent = '아이디는 4~20자 사이로 입력해주세요.';
        idMsg.style.color = 'var(--text-error)';
        idMsg.style.display = 'block';
        idCheckStatus = false;
        return;
    }

    try {
        const response = await fetch(`/api/member/check-id?username=${encodeURIComponent(id)}`);
        const message = await response.text();

        if (response.ok) {
            idMsg.textContent = message;
            idMsg.style.color = 'var(--brand-primary)';
            idCheckStatus = true;
        } else {
            idMsg.textContent = message;
            idMsg.style.color = 'var(--text-error)';
            idCheckStatus = false;
        }
        idMsg.style.display = 'block';

    } catch (error) {
        console.error(error);
        idMsg.textContent = '중복 확인 중 오류가 발생했습니다.';
        idMsg.style.color = 'var(--text-error)';
        idMsg.style.display = 'block';
        idCheckStatus = false;
    }
});
signupIdInput.addEventListener('input', () => {
    idCheckStatus = false;
    idMsg.style.display = 'none';
});

// (기타 유효성 검사 UI 생략)
const passMsg = document.getElementById('pass-msg');
const passConfirmMsg = document.getElementById('pass-confirm-msg');
signupPasswordInput.addEventListener('input', () => {
    const pass = signupPasswordInput.value;
    const passRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
    if (!passRegex.test(pass)) {
        passMsg.textContent = '문자, 숫자, 특수문자를 포함해 8~20자로 입력해주세요.';
        passMsg.style.display = 'block';
    } else {
        passMsg.style.display = 'none';
    }
});
signupPasswordConfirmInput.addEventListener('input', () => {
    const pass = signupPasswordInput.value;
    const confirmPass = signupPasswordConfirmInput.value;
    if (pass !== confirmPass) {
        passConfirmMsg.style.display = 'block';
    } else {
        passConfirmMsg.style.display = 'none';
    }
});

// (주소 검색 시뮬레이션)
document.getElementById('address-search-btn').addEventListener('click', () => {
    signupAddressInput.value = '서울시 강남구 테헤란로 (시뮬레이션)';
    showCustomAlert('주소 검색이 완료되었습니다 (테스트)', 'info');
});


// --- [v3~v7] 기타 로직 (검색, 상세, 푸터 충돌) ---

// (검색, 카테고리, 상세 뷰 로직 등은 v10과 동일하며, itemId 사용에 맞춰 수정되어 있습니다.)
// ... (생략) ...

// --- [v10] 페이지 최초 로드 ---
document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
});