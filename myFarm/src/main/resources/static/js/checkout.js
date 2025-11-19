// --- 맞춤형 알림창 (간소화) ---
function showCustomAlert(message, type = 'info') {
    // (기존 코드와 동일)
    const alertBox = document.createElement('div');
    alertBox.innerHTML = `<span>${type === 'success' ? '✅' : '❌'}</span> ${message}`;
    alertBox.style.cssText = `
        position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
        background-color: ${type === 'success' ? 'var(--brand-primary)' : 'var(--text-error)'};
        color: white; padding: 15px 25px; border-radius: 8px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1); z-index: 1001; font-weight: 600;
      `;
    document.body.appendChild(alertBox);
    setTimeout(() => { alertBox.remove(); }, 3000);
}

// --- 페이지 로드 시 데이터 채우기 (DB 연동) ---

/**
 * [신규] 서버 API를 호출하여 배송지 정보(User)와 주문할 상품 목록(Cart)을 가져옵니다.
 */
async function loadCheckoutData() {
    try {
        // [신규] 백엔드에 '주문 결제 페이지에 필요한 정보'를 요청하는 API
        // 이 API는 1. 로그인한 사용자의 정보 2. 사용자의 장바구니 목록을 반환해야 합니다.
        const response = await fetch('/api/order/checkout-info');

        if (!response.ok) {
            // 401(미인증)이거나 404(장바구니 비었음) 등
            if (response.status === 401) {
                alert('로그인이 필요합니다. 메인 페이지로 이동합니다.');
            } else {
                alert('주문 정보가 올바르지 않습니다. 메인 페이지로 이동합니다.');
            }
            window.location.href = '/';
            return;
        }

        const data = await response.json();

        // [신규] data 객체가 { user: {...}, cartItems: [...] } 형태라고 가정합니다.
        const user = data.user;
        const cartItems = data.cartItems;

        // 1. 배송지 정보 렌더링
        renderShippingInfo(user);

        // 2. 주문 요약 정보 렌더링
        renderOrderSummary(cartItems);

    } catch (error) {
        console.error('주문 정보 로딩 실패:', error);
        alert('주문 정보를 불러오는 데 실패했습니다.');
        window.location.href = '/';
    }
}

/**
 * [신규] 배송지 정보 UI를 채웁니다.
 * (백엔드에서 USERS, ADDRESS 테이블을 조합하여 내려준 정보를 사용)
 */
function renderShippingInfo(user) {
    // (필드명은 백엔드 DTO에 맞춰야 함)
    document.getElementById('co-name').value = user.userName || '';
    document.getElementById('co-phone').value = user.phone || '';

    // [중요] 주소는 ADDRESS 테이블에서 가져온 '기본 배송지' 주소여야 합니다.
    document.getElementById('co-address').value = user.address || '배송지 정보 없음';
}

/**
 * [신규] 주문 요약 UI를 채웁니다.
 * (백엔드 CART 테이블에서 가져온 정보를 사용)
 */
function renderOrderSummary(items) {
    const itemsList = document.getElementById('summary-items-list');
    const totalPriceEl = document.getElementById('summary-total-price');
    let total = 0;

    itemsList.innerHTML = ''; // 비우기

    if (!items || items.length === 0) {
        itemsList.innerHTML = '<p>주문할 상품이 없습니다.</p>';
        document.getElementById('final-pay-btn').disabled = true; // 결제 버튼 비활성화
        return;
    }

    items.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('order-summary-item');

        // (필드명은 백엔드 DTO에 맞춰야 함: e.g., CartViewDTO)
        const itemTotalPrice = item.price * item.amount;
        itemDiv.innerHTML = `
          <span class="item-name">${item.itemName} <span>x ${item.amount}</span></span>
          <span class="item-price">₩${itemTotalPrice.toLocaleString()}</span>
        `;
        itemsList.appendChild(itemDiv);
        total += itemTotalPrice;
    });

    totalPriceEl.textContent = `₩${total.toLocaleString()}`;
}


// --- 결제 수단 선택 (기존과 동일) ---
const paymentMethods = document.querySelectorAll('.payment-method');
paymentMethods.forEach(method => {
    method.addEventListener('click', () => {
        paymentMethods.forEach(m => m.classList.remove('active'));
        method.classList.add('active');
    });
});

// --- 최종 결제하기 버튼 (DB 연동) ---
document.getElementById('final-pay-btn').addEventListener('click', async () => {

    // [수정] localStorage 대신 현재 UI에서 결제 정보를 가져옴
    const selectedPayment = document.querySelector('.payment-method.active').dataset.method;

    // (선택사항) 배송지 정보를 사용자가 수정했을 경우, 해당 정보를 같이 보낼 수 있습니다.
    const shippingInfo = {
        name: document.getElementById('co-name').value,
        phone: document.getElementById('co-phone').value,
        address: document.getElementById('co-address').value
    };

    // [신규] 백엔드에 '주문 생성'을 요청하는 API
    try {
        const response = await fetch('/api/order/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                paymentMethod: selectedPayment,
                // shippingInfo: shippingInfo // (선택사항) 배송지 정보를 보낼 경우
            })
        });

        if (response.ok) {
            // [삭제] localStorage.removeItem('orderCart') 등 로컬스토리지 조작 제거

            showCustomAlert('결제가 완료되었습니다! 3초 후 메인 페이지로 이동합니다.', 'success');
            setTimeout(() => {
                window.location.href = '/';
            }, 3000);

        } else {
            const errorText = await response.text();
            showCustomAlert(`주문 생성 실패: ${errorText}`, 'error');
        }

    } catch (error) {
        console.error('결제 요청 실패:', error);
        showCustomAlert('결제 중 오류가 발생했습니다.', 'error');
    }
});


// --- 페이지 최초 로드 ---
document.addEventListener('DOMContentLoaded', () => {
    // [수정] localStorage.getItem 대신, 서버에서 데이터를 로드하는 함수 호출
    loadCheckoutData();
});