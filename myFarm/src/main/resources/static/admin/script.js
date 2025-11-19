// script.js

// ======================================
// 1. API 기본 설정 및 전역 상수 / 더미 데이터
// ======================================
const API_BASE_URL = '/admin/shop';

// 상품 더미 데이터 (API 실패 시 fallback용, DB 구조 맞춤)
let products = [
    { itemId: 101, itemName: "유기농 방울토마토", price: 12000, status: 1, storId: 101 },
    { itemId: 102, itemName: "신선한 상추", price: 5000, status: 0, storId: 102 }
];

// 농가/작물/주문 더미 (일부 화면에서 사용 또는 fallback)
let farms = [
    { id: 1, name: "행복농장", owner: "홍길동", address: "서울시 강서구", phone: "010-1234-5678", account: "우리은행 1002-123-456789" }
];

let crops = [
    { id: 1, name: "방울토마토", quantity: "500kg", sowingDate: "2025-09-15", status: "재배중", isActive: true }
];

let orders = [
    { id: 'ORD-001', customer: '김고객', date: '2025-11-05', total: '24,000원', status: 'ready', products: [{ name: '유기농 방울토마토', qty: 2, price: 12000 }] },
    { id: 'ORD-002', customer: '이고객', date: '2025-11-06', total: '50,000원', status: 'paid', products: [{ name: '신선한 상추', qty: 10, price: 5000 }] }
];

// ======================================
// 2. 창고(Inventory) API 연동 및 Select Box 옵션
// ======================================

// 인벤토리 목록을 API에서 가져오는 함수
async function fetchInventoryItems() {
    try {
        const response = await fetch('/admin/api/inventory');
        if (!response.ok) {
            throw new Error(`인벤토리 API 호출 실패: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error("인벤토리 항목 로딩 오류:", error);
        alert('창고 품목을 불러오는 중 오류가 발생했습니다.');
        return [];
    }
}

async function loadInventoryOptions() {
    const selectElement = document.getElementById('new-stor-select');
    if (!selectElement) return;

    const inventoryItems = await fetchInventoryItems();

    selectElement.innerHTML = '<option value="" disabled selected>창고 품목을 선택하세요</option>';

    if (inventoryItems.length === 0) {
        const option = document.createElement('option');
        option.disabled = true;
        option.textContent = "등록된 인벤토리 품목이 없습니다.";
        selectElement.appendChild(option);
        return;
    }

    inventoryItems.forEach(item => {
        const option = document.createElement('option');
        option.value = item.storId;
        option.textContent = `${item.storName} (ID: ${item.storId}, 재고: ${item.amount || 0}개)`;
        selectElement.appendChild(option);
    });
}

// ======================================
// 3. 공통 UI 유틸 (모달 / 탭)
// ======================================

// 모달 열기
function openModal(modalId, itemId = null) {
    document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
    const modal = document.getElementById(modalId);
    if (!modal) return;

    const idDisplay = document.getElementById(modalId.replace('modal', 'id-display'));
    if (idDisplay) idDisplay.textContent = itemId ?? '';

    // 주문 상세 모달
    if (modalId === 'order-detail-modal' && itemId) {
        modal.dataset.orderId = itemId;
        populateOrderDetailModal(itemId);
    }

    // 작물 수정 모달 (단건 조회)
    if (modalId === 'edit-crop-modal' && itemId != null) {
        loadCropIntoEditForm(itemId).catch(() => {
            alert('농작물 정보를 불러오지 못했습니다.');
        });
    }

    // 상품 수정 모달 (단건 조회)
    if (modalId === 'edit-product-modal' && itemId != null) {
        populateEditForm(modalId, itemId);
    }

    // 새 상품 등록 모달: 창고 SelectBox 옵션 로드
    if (modalId === 'new-product-modal') {
        loadInventoryOptions();
    }

    modal.style.display = 'block';
}

// 모달 닫기
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// 모달 외부 클릭 시 닫기
window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
};

// 탭 전환 + 탭별 데이터 로드
function initTabFunctionality() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');

    tabContents.forEach(content => {
        content.style.display = content.classList.contains('active') ? 'block' : 'none';
    });

    tabButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            const targetId = e.currentTarget.dataset.target;

            tabButtons.forEach(btn => btn.classList.remove('active'));
            e.currentTarget.classList.add('active');

            tabContents.forEach(content => content.style.display = 'none');
            const targetContent = document.getElementById(targetId);
            if (targetContent) targetContent.style.display = 'block';

            if (targetId === 'product-manage') {
                renderProductList();
            }
            if (targetId === 'farm-manage') {
                fetchAddress().then(renderFarmAddressFromData).catch(console.error);
                fetchCrops().then(renderCropListFromData).catch(console.error);
            }
            if (targetId === 'order-manage') {
                renderOrderList();
            }
        });
    });
}

// ======================================
// 4. 농가 정보(주소) API 연동
// ======================================

async function fetchAddress() {
    const res = await fetch('/admin/api/address/', {
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
    });
    if (!res.ok) throw new Error("주소 목록 로딩 실패");
    return await res.json();
}

function renderFarmAddressFromData(addr) {
    const tbody = document.getElementById("farm-info");
    if (!tbody) return;

    if (!Array.isArray(addr) || addr.length === 0) {
        tbody.innerHTML = `
      <tr><td colspan="5" style="text-align:center;color:#888;">등록된 농가 정보가 없습니다.</td></tr>
    `;
        return;
    }

    tbody.innerHTML = addr.map(a => `
    <tr data-address-id="${a.addressId}">
      <td class="col-name">${a.addressName ?? "-"}</td>
      <td class="col-address">${a.address ?? "-"}</td>
      <td class="col-owner">${a.recipientName ?? "-"}</td>
      <td class="col-phone">${a.recipientPhone ?? "-"}</td>
      <td>
        <button class="btn-small btn-edit" onclick="openFarmEdit(this, ${a.addressId})">수정</button>
      </td>
    </tr>
  `).join("");
}

function openFarmEdit(buttonEl, addressId) {
    const tr = buttonEl.closest('tr');
    if (!tr) return;

    const name = tr.querySelector('.col-name')?.textContent?.trim() || '';
    const address = tr.querySelector('.col-address')?.textContent?.trim() || '';
    const owner = tr.querySelector('.col-owner')?.textContent?.trim() || '';
    const phone = tr.querySelector('.col-phone')?.textContent?.trim() || '';

    document.getElementById('edit-farm-id-display').textContent = String(addressId);
    document.getElementById('edit-farm-name').value = name;
    document.getElementById('edit-farm-address').value = address;
    document.getElementById('edit-farm-owner').value = owner;
    document.getElementById('edit-farm-contact').value = phone;

    openModal('edit-farm-modal', addressId);
}

async function handleEditFarmAddress(e) {
    e.preventDefault();

    const addressId = Number(document.getElementById('edit-farm-id-display').textContent || '0');

    const payload = {
        addressId: addressId,
        addressName: document.getElementById('edit-farm-name').value.trim(),
        address: document.getElementById('edit-farm-address').value.trim(),
        recipientName: document.getElementById('edit-farm-owner').value.trim(),
        recipientPhone: document.getElementById('edit-farm-contact').value.trim()
    };

    if (!payload.addressName || !payload.address) {
        alert('농가명과 주소는 필수입니다.');
        return;
    }

    try {
        const res = await fetch('/admin/api/address/update', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const text = await res.text().catch(() => '');
        if (!res.ok || text !== 'success') throw new Error(text || '업데이트 실패');

        const list = await fetchAddress();
        renderFarmAddressFromData(list);

        closeModal('edit-farm-modal');
        alert('주소 정보가 수정되었습니다.');
    } catch (err) {
        alert('수정 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
}

// ======================================
// 5. 작물 재배 현황 API 연동 (/admin/api/crops)
// ======================================

async function fetchCrops() {
    const res = await fetch('/admin/api/crops', {
        headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('Failed to load crops');
    return await res.json();
}

function percentOf(crop) {
    const et = Number(crop.elapsedTick);
    const gt = Number(crop.growthTime);
    if (!Number.isFinite(et) || !Number.isFinite(gt) || gt <= 0) return '-';
    return Math.min(100, Math.floor((et / gt) * 100)) + '%';
}

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
        <td id="crop-progress-${crop.cropId}">${percentOf(crop)}</td>
        <td>
          <label class="switch">
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
            } catch (err) {
                e.target.checked = !checked;
                alert('재배상태 변경에 실패했습니다.');
            }
        });
    });
}

async function refreshCropProgressCells() {
    try {
        const cropList = await fetchCrops();
        cropList.forEach(crop => {
            const cell = document.getElementById(`crop-progress-${crop.cropId}`);
            if (cell) cell.textContent = percentOf(crop);
        });
    } catch (e) {
        // 무시 후 다음 틱에서 다시 시도
    }
}

async function loadCropIntoEditForm(cropId) {
    const res = await fetch(`/admin/api/crops/${cropId}`);
    if (!res.ok) throw new Error('crop not found');
    const crop = await res.json();

    document.getElementById('edit-crop-id-display').textContent = crop.cropId ?? '';

    const nameEl = document.getElementById('edit-crop-name');
    nameEl.value = crop.cropName ?? '';
    // 이름은 수정 못 하게
    nameEl.readOnly = true;

    document.getElementById('edit-growth-time').value = (crop.growthTime ?? 60);
    document.getElementById('edit-quantity').value = (crop.quantity ?? 0);
    document.getElementById('edit-unit-name').value = crop.unitName ?? '';
    document.getElementById('edit-reg-date').value = (crop.regDate ?? '').toString().slice(0, 10);
}

let cropProgressTimer = null;
function startCropAutoRefresh(intervalMs = 1000) {
    if (cropProgressTimer) clearInterval(cropProgressTimer);
    cropProgressTimer = setInterval(refreshCropProgressCells, intervalMs);
}

document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        if (cropProgressTimer) {
            clearInterval(cropProgressTimer);
            cropProgressTimer = null;
        }
    } else {
        startCropAutoRefresh();
        refreshCropProgressCells();
    }
});

// ======================================
// 6. 상품 목록 및 통계 / 주문 목록
// ======================================

// 주문 목록 조회 API
async function fetchOrders() {
    const res = await fetch('/admin/api/order/list', {
        headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('주문 목록 로딩 실패');
    return await res.json(); // List<OrderVO>
}

// 상품 목록 렌더 (API + fallback)
async function renderProductList() {
    const list = document.getElementById('product-list');
    if (!list) return;

    list.innerHTML = '<tr><td colspan="6">상품 데이터를 불러오는 중...</td></tr>';

    let productsToRender = [];
    try {
        const response = await fetch(API_BASE_URL);
        if (response.ok) {
            productsToRender = await response.json();
            const el = document.getElementById('summary-total-items');
            if (el) el.textContent = productsToRender.length + '개';
        } else {
            console.warn('API 호출 실패 (GET /admin/shop). 더미 데이터 사용.');
            productsToRender = products;
            const el = document.getElementById('summary-total-items');
            if (el) el.textContent = productsToRender.length + '개';
        }
    } catch (e) {
        console.error('상품 목록 로딩 오류:', e);
        productsToRender = products;
        const el = document.getElementById('summary-total-items');
        if (el) el.textContent = productsToRender.length + '개';
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

// 주문 목록 렌더 (주문 번호, 고객명, 주문일, 금액, 상태, 관리)
async function renderOrderList() {
    const list = document.getElementById('order-list');
    if (!list) return;

    list.innerHTML = '<tr><td colspan="6">주문 목록을 불러오는 중...</td></tr>';

    try {
        const orders = await fetchOrders();

        if (!Array.isArray(orders) || orders.length === 0) {
            list.innerHTML = '<tr><td colspan="6">등록된 주문이 없습니다.</td></tr>';
            return;
        }

        list.innerHTML = orders.map(order => {
            const orderId = order.orderId;
            const customerName = order.customerName ?? '-';
            const orderDate = (order.orderDate ?? '').toString().slice(0, 10);
            const total = Number(order.totalAmount ?? 0).toLocaleString() + '원';
            const status = order.status ?? '-';

            return `
        <tr data-order-id="${orderId}" data-status="${status}">
          <td>${orderId}</td>
          <td>${customerName}</td>
          <td>${orderDate}</td>
          <td>${total}</td>
          <td>${status}</td>
          <td>
            <button class="btn-small btn-detail"
                    onclick="openModal('order-detail-modal', ${orderId})">
              상세보기
            </button>
          </td>
        </tr>
      `;
        }).join('');
    } catch (e) {
        console.error(e);
        list.innerHTML =
            '<tr><td colspan="6" style="text-align:center;color:#c00;">주문 목록을 불러오지 못했습니다.</td></tr>';
    }
}

// 주문/매출 통계 요약 카드 렌더링
async function renderStatistics() {
    const totalSalesEl  = document.getElementById('summary-total-sales');
    const totalOrdersEl = document.getElementById('summary-total-orders');
    const avgOrderEl    = document.getElementById('summary-avg-order');

    // 요소가 없으면 그냥 종료
    if (!totalSalesEl || !totalOrdersEl || !avgOrderEl) return;

    try {
        const res = await fetch('/api/stats/total', {
            method: 'GET',
            headers: {
                'Content-type': 'application/json'
            }
        });

        if (!res.ok) {
            console.error('통계 API 호출 실패:', res.status);
            // 실패하면 HTML에 적혀 있던 기본값(0원/0건) 그대로 둔다.
            return;
        }

        const data = await res.json();
        // 기대 JSON 형식:
        // { "totalSales": 170000, "totalOrders": 5, "totalAvg": 34000 }

        totalSalesEl.textContent  = `${Number(data.totalSales ?? 0).toLocaleString()}원`;
        totalOrdersEl.textContent = `${Number(data.totalOrders ?? 0).toLocaleString()}건`;
        avgOrderEl.textContent    = `${Number(data.totalAvg ?? 0).toLocaleString()}원`;
    } catch (err) {
        console.error('통계 로딩 중 오류:', err);
        // 에러가 나도 화면은 0원/0건 유지
    }
}


function renderAllLists() {
    renderProductList();
    renderOrderList();
    renderStatistics();
}

// ======================================
// 7. 등록(Create) 핸들러
// ======================================

function handleNewFarm(e) {
    e.preventDefault();
    const name = document.getElementById('farm-name')?.value || '새 농가';
    const owner = document.getElementById('farm-owner')?.value || '미지정';
    const account = document.getElementById('farm-account')?.value || '계좌 미등록';

    farms.push({
        id: Date.now(),
        name,
        owner,
        account,
        address: "주소 미입력",
        phone: "연락처 미입력"
    });

    alert(`농가 '${name}' 등록 완료 (DB INSERT 필요)`);
    closeModal('new-farm-modal');
}

async function handleNewCrop(e) {
    e.preventDefault();

    const name = document.getElementById('crop-name')?.value?.trim();
    const quantity = parseInt(document.getElementById('quantity')?.value || '0', 10) || 0;
    const unitName = document.getElementById('unit-name')?.value?.trim() || '';
    const regDate = document.getElementById('reg-date')?.value || null;
    const statusSel = document.getElementById('status')?.value;
    const gtRaw = document.getElementById('growth-time')?.value || '';
    let growthTime = parseInt(gtRaw, 10);
    if (!Number.isFinite(growthTime) || growthTime <= 0) growthTime = 60;

    if (!name) {
        alert('농작물명을 입력하세요.');
        return;
    }

    const payload = {
        cropName: name,
        quantity: quantity,
        unitName: unitName,
        regDate: regDate,
        status: (statusSel === 'enable') ? 1 : 0,
        growthTime: growthTime,
        elapsedTick: 0
    };

    try {
        const res = await fetch('/admin/api/crops', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error(await res.text().catch(() => '등록 실패'));

        const list = await fetchCrops();
        renderCropListFromData(list);
        closeModal('new-crop-modal');
        document.getElementById('new-crop-form')?.reset();
        alert('농작물이 등록되었습니다.');
    } catch (err) {
        alert('등록 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
}

async function handleNewProduct(e) {
    e.preventDefault();

    const itemName = document.getElementById('new-item-name').value;
    const price = parseInt(document.getElementById('new-item-price').value || 0);
    const storId = document.getElementById('new-stor-select').value;

    if (!storId) {
        alert('농작물을 선택해주세요.');
        return;
    }

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
// 8. 수정(Update) 및 삭제(Delete)
// ======================================

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

            const storIdEl = document.getElementById('edit-stor-id');
            storIdEl.value = item.storId || '';
            // 창고 매핑은 수정 불가
            storIdEl.disabled = true;

        } catch (error) {
            console.error('데이터 로드 오류:', error);
            alert('수정할 상품 데이터를 불러오는 데 실패했습니다.');
        }
    }
}

function handleEditCrop(e) {
    e.preventDefault();
    const cropId = document.getElementById('edit-crop-id-display').textContent;

    const name = document.getElementById('edit-crop-name')?.value?.trim();
    const gtRaw = document.getElementById('edit-growth-time')?.value;
    const qtyRaw = document.getElementById('edit-quantity')?.value;
    const unitName = document.getElementById('edit-unit-name')?.value?.trim();
    const regDate = document.getElementById('edit-reg-date')?.value || null;

    const growthTime = gtRaw === '' ? null : Number(gtRaw);
    const quantity = qtyRaw === '' ? null : Number(qtyRaw);

    const payload = {
        cropId: Number(cropId),
        cropName: (name && name.length > 0) ? name : null,
        growthTime: Number.isFinite(growthTime) ? growthTime : null,
        quantity: Number.isFinite(quantity) ? quantity : null,
        unitName: (unitName && unitName.length > 0) ? unitName : null,
        regDate: regDate || null
    };

    fetch(`/admin/api/crops/${cropId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async res => {
            if (!res.ok) throw new Error(await res.text().catch(() => '수정 실패'));
            return fetchCrops();
        })
        .then(list => {
            renderCropListFromData(list);
            closeModal('edit-crop-modal');
            alert('농작물 정보가 수정되었습니다.');
        })
        .catch(err => {
            alert('수정 중 오류가 발생했습니다.\n' + (err?.message || ''));
        });
}

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

async function handleStatusToggle(itemId, isChecked) {
    const newStatus = isChecked ? 1 : 0;

    try {
        const response = await fetch(API_BASE_URL + '/status/' + itemId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (!response.ok) {
            alert('상태 변경 실패! 서버 응답을 확인하세요.');
            renderProductList();
        }
    } catch (error) {
        console.error('상태 변경 통신 오류:', error);
        alert('판매 상태 변경 중 통신 오류가 발생했습니다.');
        renderProductList();
    }
}

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

async function handleDelete(type, id) {
    const label = (type === 'crop' ? '농작물' : '농가');
    if (!confirm(`${label} ID: ${id}을(를) 정말 삭제하시겠습니까?`)) return;

    if (type === 'crop') {
        try {
            const res = await fetch(`/admin/api/crops/${id}`, { method: 'DELETE' });
            if (!res.ok && res.status !== 204) {
                const msg = await res.text().catch(() => '');
                throw new Error(msg || '삭제 실패');
            }

            const tr = document.querySelector(`#crop-list tr[data-id="${id}"]`);
            if (tr) tr.remove();

            const list = await fetchCrops();
            renderCropListFromData(list);

            alert('삭제되었습니다.');
        } catch (err) {
            alert('삭제 중 오류가 발생했습니다.\n' + (err?.message || ''));
        }
        return;
    }

    if (type === 'farm') {
        farms = farms.filter(f => f.id !== id);
        alert('농가가 삭제되었습니다. (DB DELETE 필요)');
        return;
    }
}

// ======================================
// 9. 주문 상세 모달 / 상태 변경
// ======================================

// 주문 상태 → 라벨/클래스 매핑
function mapOrderStatus(status) {
    const s = String(status ?? '').trim();

    if (s === '주문 대기') {
        return { label: '주문 대기', className: 'status-pending' };
    }
    if (s === '배송 중') {
        return { label: '배송 중', className: 'status-shipping' };
    }
    if (s === '결제 완료') {
        return { label: '결제 완료', className: 'status-paid' };
    }
    if (s === '주문 취소') {
        return { label: '주문 취소', className: 'status-cancelled' };
    }

    return { label: s || '기타', className: 'status-etc' };
}

// 주문 상세 모달 채우기 (API 기반)
async function populateOrderDetailModal(orderId) {
    try {
        const res = await fetch(`/admin/api/order/${orderId}`, {
            headers: { 'Content-Type': 'application/json' }
        });
        if (!res.ok) throw new Error('주문 상세를 불러오지 못했습니다.');

        const dto = await res.json();

        document.getElementById('order-detail-title').textContent =
            `주문 상세 정보 (${orderId})`;

        const customerName =
            dto.customerName ??
            dto.recipientName ??
            dto.ordRecipientName ??
            '-';
        document.getElementById('detail-customer-name').textContent = customerName;

        const orderDate = (dto.orderDate ?? dto.createdAt ?? '').toString().slice(0, 10);
        document.getElementById('detail-order-date').textContent =
            orderDate || '-';

        const totalAmount = Number(dto.totalAmount ?? 0);
        document.getElementById('detail-total-amount').textContent =
            totalAmount.toLocaleString() + '원';

        const statusInfo = mapOrderStatus(dto.status);
        const badge = document.getElementById('detail-order-status-badge');
        badge.textContent = statusInfo.label;
        badge.className = `status-badge ${statusInfo.className}`;

        const statusSelect = document.getElementById('detail-status-select');
        const statusBtn = document.querySelector('.status-change button');

        if (statusSelect) {
            statusSelect.value = dto.status || '주문 대기';
        }

        if (dto.status === '주문 취소') {
            if (statusSelect) statusSelect.disabled = true;
            if (statusBtn) {
                statusBtn.disabled = true;
                statusBtn.textContent = '취소된 주문';
            }
        } else {
            if (statusSelect) statusSelect.disabled = false;
            if (statusBtn) {
                statusBtn.disabled = false;
                statusBtn.textContent = '변경';
            }
        }

        const productList = document.getElementById('detail-product-list');
        const items = Array.isArray(dto.orderAmountList) ? dto.orderAmountList : [];

        if (items.length === 0) {
            productList.innerHTML = '<li>주문 상품이 없습니다.</li>';
        } else {
            productList.innerHTML = items.map(p => {
                const name = p.itemName ?? '-';
                const qty = Number(p.quantity ?? p.amount ?? 0);
                const unitPrice = Number(p.unitPrice ?? p.price ?? 0);
                const sum = (qty * unitPrice).toLocaleString();
                return `<li>${name} (${qty}개) - ${sum}원</li>`;
            }).join('');
        }
    } catch (err) {
        alert('주문 상세 조회 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
}

// 주문 상태 변경 요청
async function handleOrderStatusChange() {
    const modal = document.getElementById('order-detail-modal');
    if (!modal) return;

    const orderId = modal.dataset.orderId;
    if (!orderId) {
        alert('주문 번호를 찾을 수 없습니다.');
        return;
    }

    const statusSelect = document.getElementById('detail-status-select');
    if (!statusSelect) return;

    const newStatus = statusSelect.value;

    if (!confirm(`주문 ${orderId}의 상태를 '${newStatus}'(으)로 변경하시겠습니까?`)) {
        return;
    }

    try {
        const res = await fetch(`/admin/api/order/${orderId}/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        const text = await res.text().catch(() => '');
        if (!res.ok || text !== 'success') {
            throw new Error(text || '상태 변경 실패');
        }

        const info = mapOrderStatus(newStatus);
        const badge = document.getElementById('detail-order-status-badge');
        badge.textContent = info.label;
        badge.className = `status-badge ${info.className}`;

        alert('주문 상태가 변경되었습니다.');

        // 방금 '주문 취소'로 변경했다면 즉시 UI 잠그기
        if (newStatus === '주문 취소') {
            statusSelect.disabled = true;
            const statusBtn = document.querySelector('.status-change button');
            if (statusBtn) {
                statusBtn.disabled = true;
                statusBtn.textContent = '취소된 주문';
            }
        }

        if (typeof renderOrderList === 'function') {
            renderOrderList();
        }
    } catch (err) {
        alert('상태 변경 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
}

// ======================================
// 11. 월별 매출 Chart.js 라인 그래프
// ======================================

// 전역 Chart 인스턴스 보관용
let monthlySalesChart = null;

// 월별 통계 데이터 불러와서 그래프 그리기
async function loadMonthlySalesChart() {
    const canvas = document.getElementById('monthly-sales-chart');
    if (!canvas) {
        console.warn('[stats] #monthly-sales-chart 캔버스를 찾을 수 없습니다.');
        return;
    }

    try {
        const res = await fetch('/api/stats/monthly', {
            headers: { 'Accept': 'application/json' }
        });

        if (!res.ok) {
            throw new Error('월별 통계 API 호출 실패: ' + res.status);
        }

        // [{ monthlyOrder, monthlyCount, monthlyTotal, monthlyAvg }, ...]
        const data = await res.json();

        if (!Array.isArray(data) || data.length === 0) {
            console.warn('[stats] 월별 통계 데이터가 없습니다.');
            return;
        }

        const labels        = data.map(item => item.monthlyOrder);   // "2025-07" ...
        const monthlyTotals = data.map(item => item.monthlyTotal);   // 총 매출액
        const monthlyAvgs   = data.map(item => item.monthlyAvg);     // 평균 주문액
        const monthlyCounts = data.map(item => item.monthlyCount);   // 주문 건수

        // ✅ 라벨 개수 기반으로 캔버스 가로 길이 설정
        const minWidthPerLabel = 80; // 한 달당 80px
        canvas.width = Math.max(labels.length * minWidthPerLabel, 600);

        const ctx = canvas.getContext('2d');

        if (monthlySalesChart) {
            monthlySalesChart.destroy();
        }

        monthlySalesChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: '월별 총 매출액',
                        data: monthlyTotals,
                        borderColor: 'rgba(75, 192, 192, 1)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        borderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        yAxisID: 'y'        // 왼쪽 축(금액)
                    },
                    {
                        label: '월별 평균 주문액',
                        data: monthlyAvgs,
                        borderColor: 'rgba(255, 99, 132, 1)',
                        backgroundColor: 'rgba(255, 99, 132, 0.15)',
                        borderWidth: 2,
                        pointRadius: 4,
                        pointHoverRadius: 6,
                        yAxisID: 'y'        // 왼쪽 축(금액)
                    },
                    {
                        label: '월별 주문 건수',
                        type: 'bar',
                        data: monthlyCounts,
                        yAxisID: 'y2',

                        // 막대 스타일
                        backgroundColor: 'rgba(54, 162, 235, 0.5)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1,
                        borderRadius: 4,
                        barThickness: 10,

                        // 막대에선 필요 없는 옵션들 삭제/무효
                        pointRadius: 0,
                        pointHoverRadius: 0,
                        tension: 0
                    }
                ]
            },
            options: {
                responsive: false,              // ✅ 캔버스 width 그대로 사용
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = context.parsed.y || 0;
                                const label = context.dataset.label || '';
                                if (label.includes('건수')) {
                                    return `${label}: ${value.toLocaleString()}건`;
                                }
                                return `${label}: ${value.toLocaleString()}원`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: '월 (YYYY-MM)'
                        }
                    },
                    y: { // 금액 축 (왼쪽)
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '금액(원)'
                        },
                        ticks: {
                            callback: function (value) {
                                return value.toLocaleString() + '원';
                            }
                        }
                    },
                    y2: { // 주문 건수 축 (오른쪽)
                        position: 'right',
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '주문 건수(건)'
                        },
                        grid: {
                            drawOnChartArea: false
                        },
                        ticks: {
                            stepSize: 1,
                            callback: function (value) {
                                return value.toLocaleString() + '건';
                            }
                        }
                    }
                }
            }
        });

    } catch (e) {
        console.error('[stats] 월별 매출 차트 로딩 실패:', e);
    }
}

// ======================================
// 10. 초기화 (DOMContentLoaded)
// ======================================

document.addEventListener('DOMContentLoaded', () => {
    initTabFunctionality();
    renderAllLists();
    loadMonthlySalesChart();

    fetchAddress()
        .then(renderFarmAddressFromData)
        .catch(() => {
            const tbody = document.getElementById('farm-info');
            if (tbody) {
                tbody.innerHTML = `
          <tr><td colspan="5" style="text-align:center;color:#c00;">
            농가 정보를 불러오지 못했습니다.
          </td></tr>`;
            }
        });

    fetchCrops()
        .then(cropList => {
            renderCropListFromData(cropList);
            startCropAutoRefresh(1000);
            refreshCropProgressCells();
        })
        .catch(() => {
            // 실패 시 fallback 필요하면 여기서 처리
        });

    document.getElementById('new-farm-form')?.addEventListener('submit', handleNewFarm);
    document.getElementById('new-crop-form')?.addEventListener('submit', handleNewCrop);
    document.getElementById('new-product-form')?.addEventListener('submit', handleNewProduct);

    document.getElementById('edit-farm-form')?.addEventListener('submit', handleEditFarmAddress);
    document.getElementById('edit-crop-form')?.addEventListener('submit', handleEditCrop);
    document.getElementById('edit-product-form')?.addEventListener('submit', handleEditProduct);
});