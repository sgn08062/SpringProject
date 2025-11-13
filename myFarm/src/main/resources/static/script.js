// script.js
// ======================================
// 1. API 기본 설정 및 전역 상수
// ======================================
const API_BASE_URL = '/admin/shop';

// ⭐ DB 구조에 맞게 STATUS(0/1) 및 STOR_ID 반영
let products = [
    { itemId: 101, itemName: "유기농 방울토마토", price: 12000, status: 1, storId: 101 },
    { itemId: 102, itemName: "신선한 상추", price: 5000, status: 0, storId: 102 }
];

// ⭐ Dashboard의 다른 탭을 위한 더미 데이터 (유지)
let farms = [{ id: 1, name: "행복농장", owner: "홍길동" }];
let crops = [{ id: 1, name: "방울토마토", area: "500㎡", sowingDate: "2025-09-15", expectedHarvest: "2025-12-01", status: "재배중", isActive: true }];
let orders = [
    { id: 'ORD-001', customer: '김고객', date: '2025-11-05', total: '24,000원', status: 'ready', products: [{ name: '유기농 방울토마토', qty: 2, price: 12000 }] },
    { id: 'ORD-002', customer: '이고객', date: '2025-11-06', total: '50,000원', status: 'paid', products: [{ name: '신선한 상추', qty: 10, price: 5000 }] }
];


// ======================================
// 2. 전역 유틸리티 함수 (ReferenceError 방지)
// ⚠️ 이 함수들은 HTML의 onclick에서 직접 호출되므로 전역에 정의되어야 합니다.
// ======================================

// 모달(팝업) 열기
function openModal(modalId, itemId = null) {
    document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
    const modal = document.getElementById(modalId);
    if (modal) {
        if (modalId === 'order-detail-modal' && itemId) {
            populateOrderDetailModal(itemId);
        }

        const idDisplay = document.getElementById(modalId.replace('modal', 'id-display'));
        if(idDisplay) idDisplay.textContent = itemId;

        // 수정 모달 열 때 데이터 채우기
        if (modalId.startsWith('edit-')) {
            populateEditForm(modalId, itemId);
        }

        modal.style.display = 'block';
    }
}

// 모달(팝업) 닫기
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// 탭 전환 기능
function initTabFunctionality() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabContents.forEach(content => {
        if (content.classList.contains('active')) {
            content.style.display = 'block';
        } else {
            content.style.display = 'none';
        }
    });

    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const targetId = e.target.dataset.target;

            tabButtons.forEach(btn => btn.classList.remove('active'));
            e.target.classList.add('active');

            tabContents.forEach(content => content.style.display = 'none');
            const targetContent = document.getElementById(targetId);
            if (targetContent) {
                targetContent.style.display = 'block';
            }

            // 탭 변경 시 데이터 로드
            if (targetId === 'product-manage') renderProductList();
            if (targetId === 'farm-manage') { renderFarmList(); renderCropList(); }
            if (targetId === 'order-manage') renderOrderList();

        });
    });
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}


// ======================================
// 3. 데이터 렌더링 함수
// ======================================

function renderAllLists() {
    renderFarmList();
    renderCropList();
    renderProductList();
    renderOrderList();
    renderStatistics(); // 통계 카드는 0으로 초기화
}

function renderFarmList() {
    const list = document.getElementById('farm-list');
    if (!list) return;
    list.innerHTML = farms.map(farm => `
        <tr data-id="${farm.id}"><td>${farm.name}</td><td>${farm.owner}</td><td><button class="btn-small btn-edit" onclick="openModal('edit-farm-modal', ${farm.id})">수정</button></td></tr>
    `).join('');
}

function renderCropList() {
    const list = document.getElementById('crop-list');
    if (!list) return;
    list.innerHTML = crops.map(crop => `
        <tr data-id="${crop.id}">
            <td>${crop.name}</td><td>${crop.area}</td><td>${crop.sowingDate}</td><td>${crop.expectedHarvest}</td><td>${crop.status}</td>
            <td><label class="switch"><input type="checkbox" ${crop.isActive ? 'checked' : ''}><span class="slider"></span></label></td>
            <td><button class="btn-small btn-edit" onclick="openModal('edit-crop-modal', ${crop.id})">수정</button> <button class="btn-small btn-delete" onclick="handleDelete('crop', ${crop.id})">삭제</button></td>
        </tr>
    `).join('');
}

// ⭐ renderProductList: API 호출 및 토글 반영
async function renderProductList() {
    const list = document.getElementById('product-list');
    if (!list) return;

    list.innerHTML = '<tr><td colspan="6">상품 데이터를 불러오는 중...</td></tr>';

    let productsToRender = [];
    try {
        const response = await fetch(API_BASE_URL);
        if (response.ok) {
            productsToRender = await response.json();
            document.getElementById('summary-total-items').textContent = productsToRender.length + '개';
        } else {
            console.warn('API 호출 실패 (GET /admin/shop). 더미 데이터 사용.');
            productsToRender = products;
            document.getElementById('summary-total-items').textContent = productsToRender.length + '개';
        }
    } catch (e) {
        console.error('상품 목록 로딩 오류:', e);
        productsToRender = products;
        document.getElementById('summary-total-items').textContent = productsToRender.length + '개';
    }

    if (productsToRender.length === 0) {
        list.innerHTML = '<tr><td colspan="6">등록된 상품이 없습니다.</td></tr>';
        return;
    }

    list.innerHTML = productsToRender.map(product => `
        <tr data-id="${product.itemId}">
            <td>${product.itemId}</td>
            <td>${product.itemName}</td>
            <td>${product.price ? product.price.toLocaleString() + '원' : 'N/A'}</td>
            <td>${product.storId || 'N/A'}</td>
            <td>
                <label class="switch">
                    <input type="checkbox" 
                           ${product.status === 1 ? 'checked' : ''} 
                           onchange="handleStatusToggle(${product.itemId}, this.checked)">
                    <span class="slider"></span>
                </label>
            </td>
            <td>
                <button class="btn-small btn-edit" onclick="openModal('edit-product-modal', ${product.itemId})">수정</button> 
                <button class="btn-small btn-delete" onclick="handleDeleteProduct(${product.itemId})">삭제</button>
            </td>
        </tr>
    `).join('');
}

function renderOrderList() {
    const list = document.getElementById('order-list');
    if (!list) return;
    list.innerHTML = orders.map(order => {
        const statusText = order.status === 'ready' ? '배송준비' : (order.status === 'paid' ? '결제완료' : (order.status === 'shipping' ? '배송 중' : '기타'));
        return `<tr data-order-id="${order.id}" data-status="${order.status}">
            <td>${order.id}</td><td>${order.customer}</td><td>${order.date}</td><td>${order.total}</td>
            <td><span class="status-badge status-${order.status}">${statusText}</span></td>
            <td><button class="btn-small btn-detail" onclick="openModal('order-detail-modal', '${order.id}')">상세보기</button></td>
        </tr>`;
    }).join('');
}

// ⭐ renderStatistics: 통계 API 제거로 인해 0으로 초기화만 진행
function renderStatistics() {
    document.getElementById('summary-total-sales').textContent = '0원';
    document.getElementById('summary-total-orders').textContent = '0건';
    // document.getElementById('summary-total-items')는 renderProductList에서 처리
    document.getElementById('summary-avg-order').textContent = '0원';
}


// ======================================
// 4. 등록 (Create) 핸들러 (API 호출)
// ======================================

// 더미 함수 유지
function handleNewFarm(e) {
    e.preventDefault();
    alert(`농가 등록 완료 (DB INSERT 필요)`);
    closeModal('new-farm-modal');
    // renderFarmList();
}

// 더미 함수 유지
function handleNewCrop(e) {
    e.preventDefault();
    alert(`농작물 등록 완료 (DB FIELD INSERT 필요)`);
    closeModal('new-crop-modal');
    // renderCropList();
}

// ⭐ handleNewProduct: API 호출
async function handleNewProduct(e) {
    e.preventDefault();

    const itemName = document.getElementById('new-item-name').value;
    const price = parseInt(document.getElementById('new-item-price').value || 0);
    const storId = document.getElementById('new-stor-id').value;

    const itemVO = {
        itemName: itemName,
        price: price,
        storId: storId
    };

    try {
        const response = await fetch(API_BASE_URL + '/additem', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(itemVO)
        });

        if (response.status === 201) {
            alert(`상품 '${itemName}' 등록 완료!`);
            closeModal('new-product-modal');
            document.getElementById('new-product-form').reset();
            renderProductList();
        } else {
            alert('상품 등록 실패! 서버 응답을 확인하세요.');
        }

    } catch (error) {
        console.error('등록 통신 오류:', error);
        alert('상품 등록 중 오류가 발생했습니다.');
    }
}


// ======================================
// 5. 수정 (Update) 및 상세 조회 보조 함수
// ======================================

// 수정 모달에 기존 데이터를 채우는 함수 (API 호출)
async function populateEditForm(modalId, itemId) {
    if (modalId === 'edit-product-modal') {
        try {
            const response = await fetch(API_BASE_URL + '/item/' + itemId);
            if (!response.ok) throw new Error('상세 상품 데이터를 찾을 수 없습니다.');

            const item = await response.json();

            document.getElementById('edit-item-id').value = item.itemId;
            document.getElementById('edit-product-id-display').textContent = item.itemId;

            document.getElementById('edit-item-name').value = item.itemName || '';
            document.getElementById('edit-item-price').value = item.price || 0;
            document.getElementById('edit-stor-id').value = item.storId || '';

        } catch (error) {
            console.error('데이터 로드 오류:', error);
            alert('수정할 상품 데이터를 불러오는 데 실패했습니다.');
        }
    }
    // ... (나머지 Farm/Crop 로직 유지) ...
}

// 더미 함수 유지
function handleEditFarm(e) {
    e.preventDefault();
    alert(`농가 정보 수정 완료 (DB UPDATE 필요)`);
    closeModal('edit-farm-modal');
    // renderFarmList();
}

// 더미 함수 유지
function handleEditCrop(e) {
    e.preventDefault();
    alert(`농작물 정보 수정 완료 (DB UPDATE 필요)`);
    closeModal('edit-crop-modal');
    // renderCropList();
}

// ⭐ handleEditProduct: API 호출
async function handleEditProduct(e) {
    e.preventDefault();
    const itemId = document.getElementById('edit-item-id').value;

    const newName = document.getElementById('edit-item-name').value;
    const newPrice = parseInt(document.getElementById('edit-item-price').value);
    const newStorId = document.getElementById('edit-stor-id').value;

    const itemVO = {
        itemName: newName,
        price: newPrice,
        storId: newStorId
    };

    try {
        const response = await fetch(API_BASE_URL + '/item/' + itemId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(itemVO)
        });

        if (response.ok) {
            alert(`상품 ID ${itemId} 정보 수정 완료!`);
            closeModal('edit-product-modal');
            renderProductList();
        } else {
            alert('상품 수정 실패! 서버 응답을 확인하세요.');
        }

    } catch (error) {
        console.error('수정 통신 오류:', error);
        alert('상품 수정 중 통신 오류가 발생했습니다.');
    }
}


// ======================================
// 6. 상태 토글 및 삭제 핸들러
// ======================================

// ⭐ handleStatusToggle: API 호출
async function handleStatusToggle(itemId, isChecked) {
    const newStatus = isChecked ? 1 : 0;

    try {
        const response = await fetch(API_BASE_URL + '/status/' + itemId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (response.ok) {
            console.log(`상품 ID ${itemId}의 판매 상태가 ${newStatus === 1 ? '판매중' : '판매 중지'}로 변경되었습니다.`);
        } else {
            alert('상태 변경 실패! 서버 응답을 확인하세요.');
            renderProductList();
        }

    } catch (error) {
        console.error('상태 변경 통신 오류:', error);
        alert('판매 상태 변경 중 통신 오류가 발생했습니다.');
        renderProductList();
    }
}

// ⭐ handleDeleteProduct: API 호출
async function handleDeleteProduct(itemId) {
    if (!confirm(`상품 ID: ${itemId}을(를) 정말 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(API_BASE_URL + '/item/' + itemId, {
            method: 'DELETE'
        });

        if (response.status === 204) {
            alert(`상품 ID: ${itemId} 삭제 완료.`);
            renderProductList();
        } else {
            alert('상품 삭제에 실패했습니다. (서버 오류)');
        }

    } catch (error) {
        console.error('삭제 통신 오류:', error);
        alert('상품 삭제 중 통신 오류가 발생했습니다.');
    }
}

// ... (handleDelete, populateOrderDetailModal, updateOrderStatus, handleShippingSubmit 등 기타 함수 정의 유지) ...

// ======================================
// 7. 초기화 (Initialization)
// ======================================
document.addEventListener('DOMContentLoaded', () => {
    // 공통 기능 초기화
    initTabFunctionality();

    // 목록 렌더링
    renderAllLists();

    // 등록 폼 핸들러 연결
    document.getElementById('new-product-form')?.addEventListener('submit', handleNewProduct);
    document.getElementById('new-farm-form')?.addEventListener('submit', handleNewFarm);
    document.getElementById('new-crop-form')?.addEventListener('submit', handleNewCrop);

    // 수정 폼 핸들러 연결
    document.getElementById('edit-product-form')?.addEventListener('submit', handleEditProduct);
    document.getElementById('edit-farm-form')?.addEventListener('submit', handleEditFarm);
    document.getElementById('edit-crop-form')?.addEventListener('submit', handleEditCrop);

    // 기타
    document.getElementById('shipping-form')?.addEventListener('submit', handleShippingSubmit);
});