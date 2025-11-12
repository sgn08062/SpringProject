
    // --- 맞춤형 알림창 (간소화) ---
    function showCustomAlert(message, type = 'info') {
    // index.html의 showCustomAlert와 충돌하지 않도록
    // 이 페이지 전용으로 간소화된 버전을 사용합니다.
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

    // --- 페이지 로드 시 데이터 채우기 ---
    document.addEventListener('DOMContentLoaded', () => {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    const orderCart = JSON.parse(localStorage.getItem('orderCart'));

    // 1. 유저 또는 카트 정보가 없으면 메인 페이지로 돌려보냄
    if (!currentUser || !orderCart || orderCart.length === 0) {
    alert('주문 정보가 올바르지 않습니다. 메인 페이지로 돌아갑니다.');
    window.location.href = '/';
    return;
}

    // 2. 배송지 정보 채우기 (회원가입 정보 사용)
    document.getElementById('co-name').value = currentUser.name;
    document.getElementById('co-phone').value = currentUser.phone;
    document.getElementById('co-address').value = currentUser.address;

    // 3. 주문 요약 정보 채우기
    const itemsList = document.getElementById('summary-items-list');
    const totalPriceEl = document.getElementById('summary-total-price');
    let total = 0;

    itemsList.innerHTML = ''; // 비우기
    orderCart.forEach(item => {
    const itemDiv = document.createElement('div');
    itemDiv.classList.add('order-summary-item');
    itemDiv.innerHTML = `
          <span class="item-name">${item.name} <span>x ${item.qty}</span></span>
          <span class="item-price">₩${(item.price * item.qty).toLocaleString()}</span>
        `;
    itemsList.appendChild(itemDiv);
    total += item.price * item.qty;
});

    totalPriceEl.textContent = `₩${total.toLocaleString()}`;
});

    // --- 결제 수단 선택 ---
    const paymentMethods = document.querySelectorAll('.payment-method');
    paymentMethods.forEach(method => {
    method.addEventListener('click', () => {
        paymentMethods.forEach(m => m.classList.remove('active'));
        method.classList.add('active');
    });
});

    // --- 최종 결제하기 버튼 ---
    document.getElementById('final-pay-btn').addEventListener('click', () => {
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    const orderCart = JSON.parse(localStorage.getItem('orderCart'));
    const selectedPayment = document.querySelector('.payment-method.active').dataset.method;

    // 1. "전체 주문 목록" (allOrders)을 불러옴 (없으면 빈 배열)
    let allOrders = JSON.parse(localStorage.getItem('allOrders')) || [];

    // 2. 새 주문 정보 생성
    const newOrder = {
    orderId: `FARM-${Date.now()}`, // 간단한 주문 ID
    user: {
    name: currentUser.name,
    address: currentUser.address,
    phone: currentUser.phone // [v8] 관리자 페이지용 정보 추가
},
    items: orderCart,
    totalPrice: orderCart.reduce((sum, i) => sum + i.price * i.qty, 0),
    paymentMethod: selectedPayment,
    orderDate: new Date().toISOString(),
    status: '결제완료' // [v8] 관리자 페이지용 상태
};

    // 3. "전체 주문 목록"에 새 주문 추가
    allOrders.push(newOrder);

    // 4. "전체 주문 목록"을 다시 localStorage에 저장 (관리자 페이지용)
    localStorage.setItem('allOrders', JSON.stringify(allOrders));

    // 5. 임시 장바구니 정보(orderCart) 삭제
    localStorage.removeItem('orderCart');

    // 6. 완료 알림 및 페이지 이동
    showCustomAlert('결제가 완료되었습니다! 3초 후 메인 페이지로 이동합니다.', 'success');
    setTimeout(() => {
    window.location.href = '/';
}, 3000);
});

