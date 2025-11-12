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

// --- 장바구니 로직 ---
const cart = [];
const checkoutSection = document.querySelector('.checkout');
const cartItemsList = document.querySelector('.cart-items-list');
const cartBadge = document.querySelector('.cart-badge'); 
const cartToggleBtn = document.getElementById('cart-toggle-btn'); 

function adjustQuantity(name, amount) {
  const item = cart.find(i => i.name === name);
  if (item) {
    item.qty += amount;
    if (item.qty <= 0) {
      const index = cart.findIndex(i => i.name === name);
      cart.splice(index, 1);
    }
  }
  renderCart();
}

function renderCart() {
  cartItemsList.innerHTML = '';
  let total = 0;
  const totalQty = cart.reduce((sum, item) => sum + item.qty, 0);
  if (cart.length === 0) {
    cartItemsList.innerHTML = '<p style="text-align: center; color: var(--text-secondary); margin-top: 20px;">장바구니가 비어있습니다.</p>';
  } else {
    cart.forEach(item => {
      const div = document.createElement('div');
      div.classList.add('cart-item');
      div.innerHTML = `
        <span class="item-name">${item.name}</span>
        <div class="item-controls">
          <button class="btn-decrease" data-name="${item.name}">-</button>
          <span>${item.qty}</span>
          <button class="btn-increase" data-name="${item.name}">+</button>
        </div>
        <span class="item-price">₩${(item.price * item.qty).toLocaleString()}</span>
      `;
      cartItemsList.appendChild(div);
      total += item.price * item.qty;
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

// [v5 수정] addToCart 함수 (수량 인자 추가)
function addToCart(name, price, quantity = 1) { // 기본 수량 1
  const existing = cart.find(item => item.name === name);
  if (existing) {
    existing.qty += quantity; // 전달받은 수량만큼 증가
  } else {
    cart.push({ name, price, qty: quantity }); // 전달받은 수량으로 새로 추가
  }
  renderCart();
}

// 상품 목록의 '장바구니 담기' 버튼 이벤트
document.querySelectorAll('.add-btn').forEach(button => {
  button.addEventListener('click', e => {
    const card = e.target.closest('.product-card');
    const name = card.querySelector('h3').textContent;
    const priceText = card.querySelector('.price').textContent.replace('₩', '').replace(',', '');
    const price = parseInt(priceText);
    addToCart(name, price); // 기본 수량 1
    button.textContent = '✅ 담겼어요!';
    button.style.backgroundColor = 'var(--brand-primary-dark)';
    setTimeout(() => {
      button.textContent = '장바구니 담기';
      button.style.backgroundColor = 'var(--brand-primary)';
    }, 1000);
  });
});

cartItemsList.addEventListener('click', e => {
  const target = e.target;
  if (target.classList.contains('btn-increase')) adjustQuantity(target.dataset.name, 1);
  if (target.classList.contains('btn-decrease')) adjustQuantity(target.dataset.name, -1);
});

// [v8 수정] '주문하기' 버튼 로직
const orderBtn = document.getElementById('start-order-btn');
orderBtn.addEventListener('click', () => {
  // 1. 로그인 상태 확인 (localStorage)
  const currentUser = localStorage.getItem('currentUser');
  if (!currentUser) {
    showCustomAlert('로그인이 필요합니다. 로그인 후 주문해주세요.', 'error');
    openModal(loginModal); // 로그인 창 바로 띄우기
    return;
  }

  // 2. 장바구니 확인
  if (cart.length === 0) {
    showCustomAlert('장바구니가 비어 있습니다.', 'error');
    return;
  }

  // 3. 장바구니 정보를 localStorage에 저장하고 페이지 이동
  localStorage.setItem('orderCart', JSON.stringify(cart));
  window.location.href = '/checkout'; // 'checkout.html' 페이지로 이동
});

cartToggleBtn.addEventListener('click', (e) => {
  e.preventDefault();
  document.body.classList.toggle('cart-hidden');
  // [v7] 토글 시 스크롤 핸들러 즉시 호출
  handleCartFooterCollision();
});
renderCart();

// --- [v3] 모달 로직 ---
const loginModal = document.getElementById('login-modal-overlay');
const signupModal = document.getElementById('signup-modal-overlay');
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

// --- [v3] 검색 로직 (결과 없음 메시지 포함) ---
const searchBar = document.querySelector('.search-bar input');
const allProducts = document.querySelectorAll('.product-card');
const noResultsMsg = document.getElementById('search-results-msg');
const noResultsQuery = noResultsMsg.querySelector('span');
// [v4] 상품 그리드 컨테이너 (그리드 자체)
const productSection = document.querySelector('.product-section');
// [v5 신규] 상품 상세 뷰 컨테이너
const detailView = document.getElementById('product-detail-view');

searchBar.addEventListener('input', (e) => {
  const searchTerm = e.target.value.toLowerCase().trim();
  let visibleCount = 0;
  
  allProducts.forEach(card => {
    const productName = card.querySelector('h3').textContent.toLowerCase();
    const isVisible = productName.includes(searchTerm);
    card.style.display = isVisible ? '' : 'none';
    if (isVisible) visibleCount++;
  });

  // [v3] 검색 결과 메시지 처리
  if (visibleCount === 0 && searchTerm !== '') {
    noResultsQuery.textContent = searchTerm;
    noResultsMsg.style.display = 'block';
    productSection.style.justifyContent = 'center'; // [v4] 메시지 중앙 정렬
  } else {
    noResultsMsg.style.display = 'none';
    productSection.style.justifyContent = 'flex-start'; // [v4] 카드 좌측 정렬
  }
});

// --- [v5 신규] 카테고리 필터링 로직 ---
const categoryLinks = document.querySelectorAll('.category-sidebar a');

categoryLinks.forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    
    // (1) 활성 클래스(디자인) 변경
    categoryLinks.forEach(l => l.classList.remove('active'));
    link.classList.add('active');
    
    const selectedCategory = link.dataset.category;
    
    // (2) 상품 필터링
    allProducts.forEach(card => {
      if (selectedCategory === 'all' || card.dataset.category === selectedCategory) {
        card.style.display = '';
      } else {
        card.style.display = 'none';
      }
    });

    // (3) 상세 뷰 숨기고 목록 뷰 표시 (필수)
    detailView.style.display = 'none';
    productSection.style.display = 'flex';
    noResultsMsg.style.display = 'none'; // 검색결과 메시지도 숨김
  });
});


// --- [v5 신규] 상품 상세 페이지 로직 ---
const productLinks = document.querySelectorAll('.product-link');

productLinks.forEach(link => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    
    // (1) 클릭한 상품 정보 가져오기 (data-* 속성)
    const name = link.dataset.name;
    const price = link.dataset.price;
    const imgSrc = link.dataset.imgSrc;
    const desc = link.dataset.desc;

    // (2) 상세 뷰(detailView) 내용 채우기
    detailView.innerHTML = `
      <a class="back-to-list-btn"> &lt; 목록으로 돌아가기</a>
      <div class="detail-container">
        <img src="${imgSrc}" alt="${name}" class="detail-image" onerror="this.src='https://placehold.co/400x400/E9F5E4/4A7C2C?text=${name}'">
        <div class="detail-info">
          <h2>${name}</h2>
          <div class="price">₩${price}</div>
          <p class="description">${desc}</p>
          <div class="quantity-selector">
            <label for="detail-quantity">수량:</label>
            <input type="number" id="detail-quantity" value="1" min="1">
          </div>
          <div class="detail-buttons">
            <button class="btn btn-buy" data-name="${name}" data-price="${price.replace(',', '')}">바로 구매하기</button>
            <button class="btn btn-cart" data-name="${name}" data-price="${price.replace(',', '')}">장바구니 담기</button>
          </div>
        </div>
      </div>
    `;
    
    // (3) 목록 숨기고 상세 뷰 표시
    productSection.style.display = 'none';
    detailView.style.display = 'block';
  });
});

// [v5 신규] 상세 페이지 이벤트 위임 (목록가기, 장바구니 버튼 등)
detailView.addEventListener('click', (e) => {
  const target = e.target;

  // (1) "목록으로 돌아가기" 클릭 시
  if (target.classList.contains('back-to-list-btn')) {
    detailView.style.display = 'none';
    productSection.style.display = 'flex';
  }

  // (2) 상세 뷰의 "장바구니 담기" 버튼 클릭 시
  if (target.classList.contains('btn-cart')) {
    const name = target.dataset.name;
    const price = parseInt(target.dataset.price);
    const quantity = parseInt(document.getElementById('detail-quantity').value);
    
    // 수량만큼 장바구니에 추가 (기존 addToCart 함수 수정 필요)
    addToCart(name, price, quantity); // 수량 인자 추가
    showCustomAlert(`${name} ${quantity}개를 장바구니에 담았습니다!`, 'success');
  }

  // (3) 상세 뷰의 "바로 구매하기" 버튼 클릭 시 (시뮬레이션)
  if (target.classList.contains('btn-buy')) {
    const name = target.dataset.name;
    const quantity = parseInt(document.getElementById('detail-quantity').value);
    showCustomAlert(`${name} ${quantity}개 구매 완료 (시뮬레이션)`, 'success');
  }
});


// --- [v3] 로그인/회원가입 로직 (그대로) ---
let userDatabase = []; 
let idCheckStatus = false; 

// [v8] 페이지 로드 시 localStorage에서 userDatabase 로드 (시뮬레이션 데이터 유지)
// 실제로는 DB에서 가져와야 함
if (localStorage.getItem('userDatabase')) {
  userDatabase = JSON.parse(localStorage.getItem('userDatabase'));
}

// (로그인 요소)
const beforeLoginNav = document.getElementById('before-login');
const afterLoginNav = document.getElementById('after-login');
const userGreetingSpan = document.getElementById('user-greeting');
const loginSubmitBtn = document.getElementById('login-submit-btn');
const logoutBtn = document.getElementById('logout-btn');
const loginIdInput = document.getElementById('login-id');
const loginPasswordInput = document.getElementById('login-password'); 

// (회원가입 요소)
const signupSubmitBtn = document.getElementById('signup-submit-btn');
const idCheckBtn = document.getElementById('id-check-btn');
const signupIdInput = document.getElementById('signup-id');
const signupPasswordInput = document.getElementById('signup-password');
const signupPasswordConfirmInput = document.getElementById('signup-password-confirm');
const signupNameInput = document.getElementById('signup-name');
const signupEmailIdInput = document.getElementById('signup-email-id');
const signupEmailDomainInput = document.getElementById('signup-email-domain');
const signupPhoneInput = document.getElementById('signup-phone');
const signupAddressInput = document.getElementById('signup-address');
const signupAddressDetailInput = document.getElementById('signup-address-detail');
 
// (메시지 요소)
const idMsg = document.getElementById('id-msg');
const passMsg = document.getElementById('pass-msg');
const passConfirmMsg = document.getElementById('pass-confirm-msg');

// (아이디 중복확인)
idCheckBtn.addEventListener('click', () => {
    const id = signupIdInput.value;
    if (id.length < 6 || id.length > 20) {
        idMsg.textContent = '아이디는 6~20자 사이로 입력해주세요.';
        idMsg.style.color = 'var(--text-error)';
        idMsg.style.display = 'block';
        idCheckStatus = false;
        return;
    }
    const existingUser = userDatabase.find(u => u.id === id);
    if (existingUser) {
        idMsg.textContent = '이미 사용 중인 아이디입니다.';
        idMsg.style.color = 'var(--text-error)';
        idMsg.style.display = 'block';
        idCheckStatus = false;
    } else {
        idMsg.textContent = '사용 가능한 아이디입니다.';
        idMsg.style.color = 'var(--brand-primary)';
        idMsg.style.display = 'block';
        idCheckStatus = true;
    }
});

// (아이디 입력 시)
signupIdInput.addEventListener('input', () => {
    idCheckStatus = false;
    idMsg.style.display = 'none';
});

// (비밀번호 검증)
signupPasswordInput.addEventListener('input', () => {
    const pass = signupPasswordInput.value;
    // [v6] 정규식 검증 추가
    const passRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
    if (!passRegex.test(pass)) {
        passMsg.textContent = '문자, 숫자, 특수문자를 포함해 8~20자로 입력해주세요.';
        passMsg.style.display = 'block';
    } else {
        passMsg.style.display = 'none';
    }
});

// (비밀번호 확인 검증)
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
    // 실제로는 Daum 주소 API 등을 연동해야 합니다.
    signupAddressInput.value = '서울시 강남구 테헤란로 (시뮬레이션)';
    showCustomAlert('주소 검색이 완료되었습니다 (테스트)', 'info');
});

// (로그인 버튼 클릭)
loginSubmitBtn.addEventListener('click', () => {
    const id = loginIdInput.value;
    const password = loginPasswordInput.value;

    if (id === "" || password === "") {
        showCustomAlert('아이디와 비밀번호를 입력해주세요', 'error');
        return;
    }
    const user = userDatabase.find(u => u.id === id && u.password === password);

    if (user) {
        userGreetingSpan.textContent = `${user.name}님`;
        beforeLoginNav.classList.add('hidden');
        afterLoginNav.classList.remove('hidden');
        closeModal(loginModal);
        loginIdInput.value = '';
        loginPasswordInput.value = '';
        showCustomAlert(`${user.name}님, 환영합니다!`, 'success');
        
        // [v8 신규] 로그인 성공 시 현재 유저 정보 저장
        localStorage.setItem('currentUser', JSON.stringify(user));

    } else {
        showCustomAlert('아이디 또는 비밀번호가 일치하지 않습니다.', 'error');
    }
});

// (회원가입 버튼 클릭)
signupSubmitBtn.addEventListener('click', () => {
    const id = signupIdInput.value;
    const password = signupPasswordInput.value;
    const confirmPass = signupPasswordConfirmInput.value;
    const name = signupNameInput.value;
    const email = `${signupEmailIdInput.value}@${signupEmailDomainInput.value}`;
    const phone = signupPhoneInput.value;
    // [v8] 상세주소 포함
    const address = `${signupAddressInput.value} ${signupAddressDetailInput.value}`;

    // [v6] 유효성 검사 강화
    if (!id || !password || !confirmPass || !name || !email || !phone || !signupAddressInput.value) {
        showCustomAlert('상세주소를 제외한 모든 항목을 입력해주세요.', 'error'); return;
    }
    if (!idCheckStatus) {
        showCustomAlert('아이디 중복확인을 해주세요.', 'error'); return;
    }
    const passRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
    if (!passRegex.test(password)) {
        showCustomAlert('비밀번호 형식이 올바르지 않습니다.', 'error'); return;
    }
    if (password !== confirmPass) {
        showCustomAlert('비밀번호가 일치하지 않습니다.', 'error'); return;
    }

    // [v8] userDatabase에 상세주소 포함하여 저장
    userDatabase.push({ id, password, name, email, phone, address });
    // [v8] localStorage에도 userDatabase 저장 (시뮬레이션 데이터 유지)
    localStorage.setItem('userDatabase', JSON.stringify(userDatabase));
    
    console.log('가입된 사용자 목록:', userDatabase); 

    showCustomAlert('회원가입 성공! 로그인 해주세요.', 'success');

    // (폼 비우기)
    signupIdInput.value = ''; signupPasswordInput.value = '';
    signupPasswordConfirmInput.value = ''; signupNameInput.value = '';
    signupEmailIdInput.value = ''; signupEmailDomainInput.value = '';
    signupPhoneInput.value = ''; signupAddressInput.value = '';
    signupAddressDetailInput.value = '';
    idMsg.style.display = 'none'; passMsg.style.display = 'none';
    passConfirmMsg.style.display = 'none'; idCheckStatus = false;

    closeModal(signupModal);
    openModal(loginModal);
});

// (로그아웃 버튼 클릭)
logoutBtn.addEventListener('click', (e) => {
    e.preventDefault();
    beforeLoginNav.classList.remove('hidden');
    afterLoginNav.classList.add('hidden');
    showCustomAlert('로그아웃 되었습니다.', 'info');

    // [v8 신규] 로그아웃 시 현재 유저 정보 삭제
    localStorage.removeItem('currentUser');
});

// --- [v7 신규] 장바구니-푸터 충돌 방지 로직 ---
window.addEventListener('scroll', handleCartFooterCollision);

function handleCartFooterCollision() {
  const cart = document.querySelector('.cart');
  const footer = document.querySelector('footer');
  
  // 장바구니가 없거나, 숨겨져 있거나, 푸터가 없으면 실행 중지
  if (!cart || !footer || document.body.classList.contains('cart-hidden')) {
    if(cart) cart.style.bottom = '20px'; // 숨길 땐 원위치
    return; 
  }

  const footerTop = footer.getBoundingClientRect().top; // 뷰포트 기준 푸터 상단 위치
  const viewportHeight = window.innerHeight; // 뷰포트 높이
  const cartBottomMargin = 20; // .cart의 CSS bottom 값

  // 장바구니의 절대적인 하단 위치 (뷰포트 기준)
  const cartBottomAbsolute = viewportHeight - cartBottomMargin;

  if (cartBottomAbsolute > footerTop) {
    // [충돌 발생]
    // 장바구니의 bottom 값을 푸터 상단에 맞게 "밀어올림"
    // (뷰포트 높이 - 푸터의 top 위치) = 푸터가 뷰포트 하단에서부터 차지한 높이
    const newBottom = (viewportHeight - footerTop) + cartBottomMargin;
    cart.style.bottom = `${newBottom}px`;
  } else {
    // [충돌 없음]
    // 장바구니의 bottom 값을 원래대로
    cart.style.bottom = '20px';
  }
}