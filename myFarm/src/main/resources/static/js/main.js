let detailModal = document.getElementById("detailModal");
let detailClose = document.querySelector("#detailModal .close");
let detailContentContainer = document.getElementById("detailContentContainer");

if (detailClose) {
    detailClose.onclick = function() {
        detailModal.style.display = "none";
        detailContentContainer.innerHTML = '<p style="text-align: center;">Loading...</p>';
    }
}

window.onclick = function(event) {
    if (event.target == detailModal) {
        detailModal.style.display = "none";
        detailContentContainer.innerHTML = '<p style="text-align: center;">Loading...</p>';
    }
}

let cartPushUrl = /*[[@{/cart/pushCart}]]*/ '/cart/pushCart';

// DB 정보 로드를 위한 API 경로 설정
let adminDetailApiUrl = /*[[@{/user/select-item-detail-json/}]]*/ '/user/select-item-detail-json/';
let adminImagesApiUrl = /*[[@{/admin/shop/item/}]]*/ '/admin/shop/item/';
let uploadBaseUrl = "/upload/";

function openDetailModal(itemId) {
    detailModal.style.display = "block";
    detailContentContainer.innerHTML = '<p style="text-align: center;">상품 정보를 불러오는 중...</p>';

    // 상품 상세 정보와 이미지 정보를 DB에서 비동기 요청
    Promise.all([
        // 1. 상품 상세 정보 (ShopVO)
        $.ajax({
            url: adminDetailApiUrl + itemId,
            type: 'GET'
        }),
        // 2. 상품 이미지 목록 (List<ImageVO>)
        $.ajax({
            url: adminImagesApiUrl + itemId + '/images',
            type: 'GET'
        })
    ])
        .then(([item, images]) => {

            // 메인 이미지 URL 찾기 및 서버 경로 조합
            let mainImage = images.find(img => img.imageType === 'MAIN');
            let itemImageUrl = mainImage ? uploadBaseUrl + mainImage.imageUrl : '/images/farm-logo.png';

            let stock = parseInt(item.inventoryAmount) || 0;
            let isSoldOut = stock <= 0;
            console.log('재고값: '+stock);

            // 상세 이미지 (DETAIL) 목록 HTML 생성
            let detailImagesHtml = images.filter(img => img.imageType === 'DETAIL').map(detailImg => `
                <img src="${uploadBaseUrl + detailImg.imageUrl}" alt="상세 이미지" style="width: 100%; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            `).join('');

            // 상세 이미지 섹션 표시 여부 결정
            let detailSectionHtml = '';
            if (images.filter(img => img.imageType === 'DETAIL').length > 0) {
                detailSectionHtml = `
                    <h3 style="margin-top: 15px; border-bottom: 1px solid #eee; padding-bottom: 5px;">상품 상세 정보</h3>
                    <div class="detail-images-container" style="display: flex; flex-direction: column; gap: 10px;">
                        ${detailImagesHtml}
                    </div>
                `;
            }

            // 최종 모달 콘텐츠 HTML
            let content = `
<img src="${itemImageUrl}" alt="${item.itemName} 이미지" class="detail-item-image" onerror="this.onerror=null; this.src='/img/defaultimg.png';">
<h2>${item.itemName}</h2>
<p class="detail-price">${new Intl.NumberFormat('ko-KR').format(item.price)}원 / 개</p>

<p>재고: ${stock > 0 ? stock + '개' : '품절'}</p>

<p>판매자: 관리자</p>
<hr>
<p><strong>상품 설명:</strong> ${item.description || '싱싱하고 맛있는 제철 농산물입니다. 지금 바로 만나보세요!'}</p>

${detailSectionHtml}

                <div class="btn-wrapper" style="margin-top: 20px;">
                ${!isSoldOut ? `<form action="${cartPushUrl}" method="post" class="cart-form-inline">
                <input type="hidden" name="itemId" value="${item.itemId}">
                <input type="number" name="amount" value="1" min="1" max="${stock}" style="width: 60px; margin-right: 5px;">
                <button type="submit" class="btn cart">주문하기</button> </form>`
                : `<span class="btn" style="background-color: var(--muted); cursor: default; width: 100%;">품절</span>`}
                </div>
                `;
            detailContentContainer.innerHTML = content;
        })
        .catch(error => {
            console.error("상품 상세 정보 로드 실패:", error);
            detailContentContainer.innerHTML = '<p style="text-align: center; color: red;">상품 정보를 불러오지 못했습니다. 서버 상태 및 API 경로를 확인하세요.</p>';
        });
}


let itemApiUrl = /*[[@{/user/list/search-ajax}]]*/ '/user/list/search-ajax';
let searchInput = document.getElementById('searchKeywordInput');
let productListContainer = document.getElementById('productList');

function executeSearch() {
    let keyword = searchInput.value.trim();


    let queryParams = new URLSearchParams({
        searchKeyword: keyword,
    });

    fetch(`${itemApiUrl}?${queryParams.toString()}`, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('검색 요청 실패: ' + response.statusText);
            }
            return response.json();
        })
        .then(items => {
            updateProductList(items);
        })
        .catch(error => {
            console.error("AJAX 처리 중 최종 오류:", error);
            productListContainer.innerHTML = '<p class="error-message">상품 목록을 불러오는 중 오류가 발생했습니다. (JSON 응답 문제일 수 있음)</p>';
        });
}

function updateProductList(items) {
    let htmlContent = '';

    if (items.length === 0) {
        productListContainer.innerHTML = '<p class="no-results">검색 결과가 없습니다.</p>';
        return;
    }

    items.forEach(item => {
        let isSoldOut = item.inventoryAmount == null || item.inventoryAmount <= 0;
        let priceFormatted = new Intl.NumberFormat('ko-KR').format(item.price || 0);
        let mainImage = item.images.find(img => img.imageType === 'MAIN');
        let itemImageUrl = mainImage ? uploadBaseUrl + mainImage.imageUrl : '/img/defaultimg.png';

        htmlContent += `
                <div class="crop-item">
                    <a href="javascript:void(0)" onclick="openDetailModal(${item.itemId})" class="item-image-link">
                        <div class="item-image">
                            <img src="${itemImageUrl}" alt="${item.itemName} 이미지" onerror="this.src='/img/defaultimg.png'">
                        ${!isSoldOut ? '' : '<div class="sold-out-overlay">SOLD OUT</div>'}
                        </div>
                    </a>

                    <div class="item-info">
                        <div class="flex-item-header">
                            <h2>${item.itemName || '상품명'}</h2>
                            <span class="badge">수확시기</span>
                        </div>
                        <p class="farmer-name">관리자</p>
                        <p class="price">${priceFormatted}원 / 개</p>

                        <div class="btn-wrapper">
                            ${!isSoldOut
            ? `<form action="${cartPushUrl}" method="post" class="cart-form-inline">
                                    <input type="hidden" name="itemId" value="${item.itemId}">
                                    <input type="hidden" name="amount" value="1">
                                    <button type="submit" class="btn cart">주문하기</button>
                                </form>`
            : `<span class="btn" style="background-color: var(--muted); color: var(--muted-foreground); cursor: default; width: 100%;">품절</span>`}
                        </div>
                    </div>
                </div>
            `;
    });

    productListContainer.innerHTML = htmlContent;
}

let searchForm = document.getElementById('searchForm');

if (searchForm) {
    searchForm.addEventListener('submit', function(event) {
        event.preventDefault();
        executeSearch();
    });
}

if (searchInput) {
    searchInput.addEventListener('keyup', function(event) {
        executeSearch();
    });
}

const chatApiUrl = '/api/ai/chat';

function toggleChat() {
    const chatContainer = document.getElementById('chatContainer');
    const isHidden = chatContainer.style.display === 'none' || chatContainer.style.display === '';

    chatContainer.style.display = isHidden ? 'flex' : 'none';

    if (isHidden) {
        document.getElementById('chatInput').focus();
    }
}

function handleEnter(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
}

function appendMessage(text, sender) {
    const chatMessages = document.getElementById('chatMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${sender}`;
    msgDiv.innerHTML = text;
    chatMessages.appendChild(msgDiv);

    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    const sendBtn = document.getElementById('sendBtn');

    if (!message) return;

    appendMessage(message, 'user');
    input.value = '';

    // 로딩 상태 처리
    input.disabled = true;
    sendBtn.disabled = true;
    appendMessage('... AI가 답변을 생각하는 중입니다 ...', 'bot');


    fetch(chatApiUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: message })
    })
        .then(response => response.json())
        .then(data => {
            // 로딩 메시지 제거 후 AI 답변 표시
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.lastChild.remove();
            appendMessage(data.answer, 'bot');
        })
        .catch(error => {
            console.error('AJAX 오류:', error);
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.lastChild.remove();
            appendMessage("🚨 AI 서버 연결 중 오류가 발생했습니다. (백엔드 및 API Key 확인 필요)", 'bot');
        })
        .finally(() => {
            input.disabled = false;
            sendBtn.disabled = false;
            input.focus();
        });
}