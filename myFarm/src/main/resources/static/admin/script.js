// script.js

// ======================================
// 🌟 1. 더미 데이터 정의 (요청사항 반영)
// (변수 이름은 HTML에 맞추기 위해 'let'으로 유지합니다.)
// ======================================
let farms = [
    { id: 1, name: "행복농장", owner: "홍길동", address: "서울시 강서구", phone: "010-1234-5678", account: "우리은행 1002-123-456789" }
];
let crops = [
    // 'area' -> 'quantity'로 변경, 'expectedHarvest' 삭제
    { id: 1, name: "방울토마토", quantity: "500kg", sowingDate: "2025-09-15", status: "재배중", isActive: true }
];
let products = [
    { id: 101, name: "유기농 방울토마토", price: "12,000원", stock: 50, farmName: "행복농장", saleStatus: "판매중" },
    { id: 102, name: "신선한 상추", price: "5,000원", stock: 0, farmName: "푸른농장", saleStatus: "게시중단" }
];
let orders = [
    { id: 'ORD-001', customer: '김고객', date: '2025-11-05', total: '24,000원', status: 'ready', products: [{ name: '유기농 방울토마토', qty: 2, price: 12000 }] },
    { id: 'ORD-002', customer: '이고객', date: '2025-11-06', total: '50,000원', status: 'paid', products: [{ name: '신선한 상추', qty: 10, price: 5000 }] }
];


// ======================================
// 🌟 2. 초기 로드 및 공통 기능
// ======================================

document.addEventListener('DOMContentLoaded', () => {
    initTabFunctionality();
    renderAllLists();

    // ✅ 기존 더미 렌더링 대신 서버 데이터로 교체
    fetchCrops()
        .then(cropList => renderCropListFromData(cropList))
        .catch(() => {
            // 실패 시 기존 더미 데이터로 대체 렌더(선택)
            if (typeof renderCropList === 'function') renderCropList();
        });
    
    // 등록 폼 핸들러
    document.getElementById('new-farm-form')?.addEventListener('submit', handleNewFarm);
    document.getElementById('new-crop-form')?.addEventListener('submit', handleNewCrop);
    document.getElementById('new-product-form')?.addEventListener('submit', handleNewProduct);
    document.getElementById('shipping-form')?.addEventListener('submit', handleShippingSubmit);
    
    // ⭐ 수정 폼 핸들러 연결
    document.getElementById('edit-farm-form')?.addEventListener('submit', handleEditFarm);
    document.getElementById('edit-crop-form')?.addEventListener('submit', handleEditCrop);
    document.getElementById('edit-product-form')?.addEventListener('submit', handleEditProduct);
});

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
        });
    });
}

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

        // ⭐ 수정 모달 열 때 데이터 채우기
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

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}


// ======================================
// 🌟 3. 데이터 렌더링 함수 (요청사항 반영)
// ======================================

function renderAllLists() {
    renderFarmList();
    //renderCropList();
    renderProductList();
    renderOrderList();
}

// [수정됨] 농가 목록 렌더링 (주소, 연락처 추가)
function renderFarmList() {
    const list = document.getElementById('farm-list');
    if (!list) return;
    // HTML 헤더 순서: 농가명, 주소, 관리자, 연락처, 관리
    list.innerHTML = farms.map(farm => `
        <tr data-id="${farm.id}">
            <td>${farm.name}</td>
            <td>${farm.address}</td>
            <td>${farm.owner}</td>
            <td>${farm.phone}</td>
            <td><button class="btn-small btn-edit" onclick="openModal('edit-farm-modal', ${farm.id})">수정</button></td>
        </tr>
    `).join('');
}

// [수정됨] 농작물 목록 렌더링 (재배수량 변경, 예상 수확일 삭제)
function renderCropList() {
    const list = document.getElementById('crop-list');
    if (!list) return;
    // HTML 헤더 순서: 농작물명, 재배수량, 파종일, 상태, 재배상태, 관리
    list.innerHTML = crops.map(crop => `
        <tr data-id="${crop.id}">
            <td>${crop.name}</td>
            <td>${crop.quantity}</td>
            <td>${crop.sowingDate}</td>
            <td>${crop.status}</td>
            <td><label class="switch"><input type="checkbox" ${crop.isActive ? 'checked' : ''}><span class="slider"></span></label></td>
            <td><button class="btn-small btn-edit" onclick="openModal('edit-crop-modal', ${crop.id})">수정</button> <button class="btn-small btn-delete" onclick="handleDelete('crop', ${crop.id})">삭제</button></td>
        </tr>
    `).join('');
}

// ✅ 서버에서 농작물 목록 요청
async function fetchCrops() {
    const res = await fetch('/admin/api/crops', {
        headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('Failed to load crops');
    return await res.json();
}

// ✅ 받아온 목록으로 테이블 렌더
function renderCropListFromData(cropList) {
    const list = document.getElementById('crop-list');
    if (!list) return;

    list.innerHTML = cropList.map(crop => {
        const isOn = Number(crop.status) === 1 || crop.status === true || crop.status === '1';
        return `
      <tr data-id="${crop.cropId}">
        <td>${crop.cropName ?? '-'}</td>
        <td>${
            (crop.quantity ?? crop.quantity === 0 ? crop.quantity : '-') +
            (crop.unitName ? ' ' + crop.unitName : '')
        }</td>
        <td>${crop.regDate ?? '-'}</td>
        <td>${
            (typeof crop.elapsedTick === 'number' && typeof crop.growthTime === 'number')
                ? Math.min(100, Math.floor((crop.elapsedTick / Math.max(1, crop.growthTime)) * 100)) + '%'
                : '-'
        }</td>
        <td>
          <label class="switch">
            <!-- ✅ 초기 상태 반영 -->
            <input type="checkbox" class="crop-status" ${isOn ? 'checked' : ''}>
            <span class="slider"></span>
          </label>
        </td>
        <td>
          <button class="btn-small btn-edit" onclick="openModal('edit-crop-modal', ${crop.cropId})">수정</button>
          <button class="btn-small btn-delete" onclick="handleDelete('crop', ${crop.cropId})">삭제</button>
        </td>
      </tr>
    `;
    }).join('');

    // ✅ 렌더 후 스위치 이벤트 바인딩
    bindCropStatusToggles();
}

function bindCropStatusToggles() {
    document.querySelectorAll('#crop-list input.crop-status').forEach(chk => {
        chk.addEventListener('change', async (e) => {
            const tr = e.target.closest('tr');
            const cropId = tr?.dataset.id;
            const checked = e.target.checked;
            const url = checked
                ? `/admin/api/crops/enable/${cropId}`
                : `/admin/api/crops/disable/${cropId}`;

            try {
                const res = await fetch(url, { method: 'POST' });
                if (!res.ok) throw new Error();
                // (옵션) 성공 후 목록 새로고침
                // const list = await fetchCrops(); renderCropListFromData(list);
            } catch (err) {
                e.target.checked = !checked; // 실패 시 되돌리기
                alert('재배상태 변경에 실패했습니다.');
            }
        });
    });
}



function renderProductList() {
    const list = document.getElementById('product-list');
    if (!list) return;
    
    list.innerHTML = products.map(product => `
        <tr data-id="${product.id}">
            <td>${product.name}</td><td>${product.price}</td>
            <td>${product.stock > 0 ? product.stock + '개' : '<span class="stock-low">품절</span>'}</td>
            <td>${product.farmName || '정보 없음'}</td>
            <td><label class="switch"><input type="checkbox" ${product.saleStatus === '판매중' ? 'checked' : ''}><span class="slider"></span></label></td>
            <td><button class="btn-small btn-edit" onclick="openModal('edit-product-modal', ${product.id})">수정</button> <button class="btn-small btn-delete" onclick="handleDelete('product', ${product.id})">삭제</button></td>
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


// ======================================
// 🌟 4. 등록 (Create) 핸들러 (요청사항 반영)
// ======================================

// [수정됨] 새 농가 등록 (account 및 기본값 추가)
function handleNewFarm(e) {
    e.preventDefault();
    const name = document.getElementById('farm-name')?.value || '새 농가';
    const owner = document.getElementById('farm-owner')?.value || '미지정';
    const account = document.getElementById('farm-account')?.value || '계좌 미등록';
    
    farms.push({ 
        id: Date.now(), 
        name: name, 
        owner: owner, 
        account: account,
        address: "주소 미입력", // 테이블 표시용 기본값
        phone: "연락처 미입력" // 테이블 표시용 기본값
    });

    alert(`농가 '${name}' 등록 완료 (DB INSERT 필요)`);
    closeModal('new-farm-modal');
    renderFarmList();
}

// [수정됨] 새 농작물 등록 (quantity 사용, expectedHarvest 제거)
function handleNewCrop(e) {
    e.preventDefault();
    const name = document.getElementById('crop-name')?.value || '새 농작물';
    // HTML의 'area' input을 'quantity' (재배수량) 데이터로 사용
    const quantity = document.getElementById('area')?.value || 'N/A'; 
    const sowingDate = document.getElementById('sowing-date')?.value || new Date().toISOString().slice(0, 10);
    // 'expected-harvest' 값은 더 이상 수집하지 않음
    const status = document.getElementById('status')?.value || '재배중';
    
    crops.push({ 
        id: Date.now(), 
        name: name, 
        quantity: quantity, // quantity로 저장
        sowingDate: sowingDate, 
        status: status, 
        isActive: true 
        // expectedHarvest 필드 제거
    });

    alert(`농작물 '${name}' 등록 완료 (DB FIELD INSERT 필요)`);
    closeModal('new-crop-modal');
    renderCropList();
}

function handleNewProduct(e) {
    e.preventDefault();
    const name = document.getElementById('new-item-name')?.value || '새 상품';
    const price = parseInt(document.getElementById('new-item-price')?.value || 0).toLocaleString() + '원';
    const stock = parseInt(document.getElementById('new-item-stock')?.value || 0);
    const status = document.getElementById('new-item-status')?.value || '판매중';

    products.push({ id: Date.now(), name: name, price: price, stock: stock, farmName: "미지정", saleStatus: status });

    alert(`상품 '${name}' 등록 완료 (DB SHOP/INVENTORY INSERT 필요)`);
    closeModal('new-product-modal');
    renderProductList();
}


// ======================================
// ⭐ 7. 수정 (Update) 로직 추가 (요청사항 반영)
// ======================================

// [수정됨] 수정 모달 데이터 채우기
function populateEditForm(modalId, itemId) {
    let item, dataList;
    
    if (modalId === 'edit-farm-modal') {
        dataList = farms;
        item = dataList.find(d => d.id === itemId);
        if (item) {
            // 모달 HTML 폼 기준
            document.getElementById('edit-farm-name').value = item.name || '';
            document.getElementById('edit-farm-owner').value = item.owner || '';
            document.getElementById('edit-farm-account').value = item.account || ''; // 계좌 정보 채우기
        }
    } else if (modalId === 'edit-crop-modal') {
        dataList = crops;
        item = dataList.find(d => d.id === itemId);
        if (item) {
            // 모달 HTML 폼 기준
            document.getElementById('edit-crop-name').value = item.name || '';
            document.getElementById('edit-area').value = item.quantity || ''; // 'edit-area' input에 'quantity' 값을 채움
            document.getElementById('edit-sowing-date').value = item.sowingDate || '';
            // 'edit-expected-harvest'는 더 이상 사용하지 않음
            document.getElementById('edit-status').value = item.status || '재배중';
        }
    } else if (modalId === 'edit-product-modal') {
        dataList = products;
        item = dataList.find(d => d.id === itemId);
        if (item) {
            document.getElementById('edit-item-name').value = item.name || '';
            document.getElementById('edit-item-price').value = item.price ? item.price.replace(/[^0-9]/g, '') : '';
            document.getElementById('edit-item-stock').value = item.stock || 0;
            document.getElementById('edit-item-status').value = item.saleStatus || '판매중';
        }
    }
}

// [수정됨] 농가 수정 처리 (account 저장)
function handleEditFarm(e) {
    e.preventDefault(); 
    const itemId = parseInt(document.getElementById('edit-farm-id-display').textContent);
    
    const newName = document.getElementById('edit-farm-name').value;
    const newOwner = document.getElementById('edit-farm-owner').value;
    const newAccount = document.getElementById('edit-farm-account').value;
    
    const farmIndex = farms.findIndex(f => f.id === itemId);
    if (farmIndex !== -1) {
        farms[farmIndex].name = newName; 
        farms[farmIndex].owner = newOwner;
        farms[farmIndex].account = newAccount; // account 업데이트
        // address, phone은 이 모달에서 수정하지 않음
    }

    alert(`농가 ID ${itemId} 정보 수정 완료: ${newName} (DB UPDATE 필요)`);
    closeModal('edit-farm-modal');
    renderFarmList(); // 목록 새로고침
}

// [수정됨] 농작물 수정 처리 (quantity 저장, expectedHarvest 제거)
function handleEditCrop(e) {
    e.preventDefault(); 
    const itemId = parseInt(document.getElementById('edit-crop-id-display').textContent);
    
    const newName = document.getElementById('edit-crop-name').value;
    const newQuantity = document.getElementById('edit-area').value; // 'edit-area' input에서 'quantity' 값을 가져옴
    const newStatus = document.getElementById('edit-status').value;
    const newSowingDate = document.getElementById('edit-sowing-date').value;
    // 'expected-harvest'는 더 이상 수정/저장하지 않음

    const cropIndex = crops.findIndex(c => c.id === itemId);
    if (cropIndex !== -1) {
        crops[cropIndex].name = newName;
        crops[cropIndex].quantity = newQuantity; // quantity로 업데이트
        crops[cropIndex].status = newStatus;
        crops[cropIndex].sowingDate = newSowingDate;
        delete crops[cropIndex].expectedHarvest; // 기존 데이터에서 해당 속성 제거
    }

    alert(`농작물 ID ${itemId} 정보 수정 완료: ${newName} (DB UPDATE 필요)`);
    closeModal('edit-crop-modal');
    renderCropList(); // 목록 새로고침
}

// [핵심 함수 3] 상품 수정 처리
function handleEditProduct(e) {
    e.preventDefault(); 
    const itemId = parseInt(document.getElementById('edit-product-id-display').textContent);
    
    const newName = document.getElementById('edit-item-name').value;
    const newPrice = parseInt(document.getElementById('edit-item-price').value).toLocaleString() + '원';
    const newStock = parseInt(document.getElementById('edit-item-stock').value);
    const newStatus = document.getElementById('edit-item-status').value;

    const productIndex = products.findIndex(p => p.id === itemId);
    if (productIndex !== -1) {
        products[productIndex].name = newName;
        products[productIndex].price = newPrice;
        products[productIndex].stock = newStock;
        products[productIndex].saleStatus = newStatus;
    }

    alert(`상품 ID ${itemId} 정보 수정 완료: ${newName} (DB UPDATE 필요)`);
    closeModal('edit-product-modal');
    renderProductList(); // 목록 새로고침
}

// ======================================
// 🌟 6. 삭제 (Delete) 핸들러 및 기타 함수
// ======================================

function handleDelete(type, id) {
    if (!confirm(`${type === 'crop' ? '농작물' : (type === 'product' ? '상품' : '농가')} ID: ${id}을(를) 정말 삭제하시겠습니까?`)) {
        return;
    }
    
    if (type === 'crop') {
        crops = crops.filter(c => c.id !== id);
        renderCropList();
    } else if (type === 'product') {
        products = products.filter(p => p.id !== id);
        renderProductList();
    } else if (type === 'farm') {
        farms = farms.filter(f => f.id !== id);
        renderFarmList();
    }
    
    alert(`${type === 'crop' ? '농작물' : (type === 'product' ? '상품' : '농가')}이 삭제되었습니다. (DB DELETE 필요)`);
}

function populateOrderDetailModal(orderId) {
    const order = orders.find(o => o.id === orderId);
    if (!order) return;

    document.getElementById('order-detail-title').textContent = `주문 상세 정보 (${orderId})`;
    document.getElementById('detail-customer-name').textContent = order.customer;
    document.getElementById('detail-order-date').textContent = order.date;
    document.getElementById('detail-total-amount').textContent = order.total;
    
    const statusBadge = document.getElementById('detail-order-status-badge');
    const statusText = order.status === 'ready' ? '배송준비' : (order.status === 'paid' ? '결제완료' : (order.status === 'shipping' ? '배송 중' : '기타'));
    statusBadge.textContent = statusText;
    statusBadge.className = `status-badge status-${order.status}`;
    
    const productList = document.getElementById('detail-product-list');
    productList.innerHTML = order.products.map(p => `
        <li>${p.name} (${p.qty}개) - ${(p.qty * p.price).toLocaleString()}원</li>
    `).join('');

    document.getElementById('new-status').value = order.status;
    document.getElementById('tracking-number').value = ''; 
}

function updateOrderStatus(newStatus) {
    const orderId = document.getElementById('order-detail-title').textContent.match(/\((.*?)\)/)?.[1];
    if (!orderId) return;

    alert(`주문 ${orderId}의 상태가 '${newStatus}'(으)로 변경 요청되었습니다. (DB UPDATE 필요)`);
    
    const order = orders.find(o => o.id === orderId);
    if (order) {
        order.status = newStatus;
    }
    
    closeModal('order-detail-modal');
    renderOrderList();
}

function handleShippingSubmit(e) {
    e.preventDefault();
    const trackingNumber = document.getElementById('tracking-number')?.value;
    
    if (!trackingNumber) {
        alert("송장 번호를 입력해주세요.");
        return;
    }
    
    updateOrderStatus('shipping');
    alert(`송장 번호 '${trackingNumber}' 입력 완료 및 주문 상태 '배송 중'으로 변경 요청되었습니다. (DB UPDATE 필요)`);
}