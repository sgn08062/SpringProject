// script.js

// ======================================
// 1. API 기본 설정 및 전역 상수 / 더미 데이터
// ======================================
const API_BASE_URL = '/admin/shop';

// 새 상품 등록 모달을 위한 이미지 관리 객체 (전역 선언)
const newProductImages = {
    mainFile: null,      // 대표 이미지 파일 (File 객체)
    detailFiles: []      // 상세 이미지 파일 목록 (File 객체 배열)
};

const MAX_DETAIL = 5;

// 상품 수정 모달 이미지 상태
const editProductImages = {
    existingMain: null,       // { imageId, imageUrl, imageType: 'MAIN' }
    existingDetails: [],      // [{ imageId, imageUrl, imageType: 'DETAIL' }, ...]
    deleteIds: new Set(),     // 삭제할 imageId 모음
    newMainFile: null,        // 새로 선택한 대표 이미지
    newDetailFiles: [],       // 새로 추가한 상세 이미지 File[]

    reset() {
        this.existingMain = null;
        this.existingDetails = [];
        this.deleteIds = new Set();
        this.newMainFile = null;
        this.newDetailFiles = [];
    }
};

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
    // 1. 모든 모달 숨기기
    document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
    const modal = document.getElementById(modalId);
    if (!modal) return;

    // 2. ID 표시
    const idDisplay = document.getElementById(modalId.replace('modal', 'id-display'));
    if (idDisplay) idDisplay.textContent = itemId ?? '';

    // 3. 모달별 데이터 로딩 및 초기화
    if (modalId === 'order-detail-modal' && itemId) {
        modal.dataset.orderId = itemId;
        populateOrderDetailModal(itemId);
    }

    if (modalId === 'edit-crop-modal' && itemId != null) {
        loadCropIntoEditForm(itemId).catch(() => {
            alert('농작물 정보를 불러오지 못했습니다.');
        });
    }

    if (modalId === 'edit-product-modal' && itemId != null) {
        populateEditForm(modalId, itemId);
    }

    if (modalId === 'new-product-modal') {
        loadInventoryOptions();

        if (typeof newProductImages !== 'undefined' && typeof renderNewProductImages === 'function') {
            newProductImages.mainFile = null;
            newProductImages.detailFiles = [];
            renderNewProductImages();
        }
    }

    // 4. 모달 표시
    modal.style.display = 'block';

    // 5. ✅ [핵심 수정] 애니메이션 강제 재실행 및 투명도 복구
    const modalContent = modal.querySelector('.modal-content');
    if (modalContent) {
        // 이전에 적용되었던 .fadeIn 클래스를 제거합니다.
        modalContent.classList.remove('fadeIn');

        // 브라우저 리플로우(Reflow)를 강제하여 애니메이션을 재시작할 준비를 합니다.
        // 이 라인 없이는 애니메이션이 제대로 재실행되지 않을 수 있습니다.
        void modalContent.offsetWidth;

        // .fadeIn 클래스를 다시 추가하여 애니메이션을 처음부터 실행합니다.
        modalContent.classList.add('fadeIn');
    }
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

    const editCropNameEl = document.getElementById('edit-crop-name'); // 요소를 변수로 저장

    document.getElementById('edit-crop-id-display').textContent = crop.cropId ?? '';
    editCropNameEl.value = crop.cropName ?? '';

    editCropNameEl.readOnly = true;

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
// [수정] 인자로 keyword와 status를 받도록 변경
async function fetchOrders(keyword = '', status = '') {
    const params = new URLSearchParams();

    // 고객명 검색어 (customerName) 추가
    if (keyword) {
        // [주의] Controller에서 어떤 이름으로 받을지 확인하고 key를 결정하세요.
        // 여기서는 customerName으로 가정합니다.
        params.append('customerName', keyword);
    }

    // 주문 상태 (status) 필터링 값 추가
    if (status) {
        params.append('status', status);
    }

    // 쿼리 파라미터를 URL에 추가
    const url = `/admin/api/order/list?${params.toString()}`;

    const res = await fetch(url, {
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
        } else {
            console.warn('API 호출 실패 (GET /admin/shop). 더미 데이터 사용.');
            productsToRender = products;
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
      <td>${product.inventoryAmount !== undefined ? product.inventoryAmount.toLocaleString() + '개' : '연동 오류'}</td>
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
       <!--  <button class="btn-small btn-delete" onclick="handleDeleteProduct(${product.itemId})">삭제</button> -->
      </td>
    </tr>
  `).join('');
}

// 주문 목록 렌더 (주문 번호, 고객명, 주문일, 금액, 상태, 관리)
async function renderOrderList() {
    const list = document.getElementById('order-list');
    if (!list) return;

    list.innerHTML = '<tr><td colspan="6">주문 목록을 불러오는 중...</td></tr>';

    const keyword = document.getElementById('order-search-keyword')?.value || '';
    const status = document.getElementById('order-status-select')?.value || '';

    try {
        const orders = await fetchOrders(keyword, status);

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

    const form = document.getElementById('new-product-form');
    if (!form) return;

    // 1. 텍스트 데이터 수집
    const formData = new FormData(form);

    const storId   = formData.get("storId");
    const itemName = formData.get("itemName");
    const priceRaw = formData.get("price");
    const price    = parseInt(priceRaw || "0", 10);

    // 2. ✅ 이미지 데이터 수집 (전역 객체 사용)
    const mainFile = newProductImages.mainFile;
    const detailFiles = newProductImages.detailFiles;

    // 3. ✅ 필수값 체크 및 이미지 유효성 검사
    if (!storId) {
        alert("농작물을 선택해주세요.");
        return;
    }
    if (!itemName || itemName.trim().length === 0) {
        alert("상품명을 입력해주세요.");
        return;
    }
    if (isNaN(price) || price < 0) {
        alert("가격을 올바르게 입력해주세요.");
        return;
    }

    // ⭐ 대표 이미지 필수 체크
    if (!mainFile) {
        alert("대표 이미지를 반드시 추가해야 합니다.");
        return;
    }

    // 숫자로 정제해서 다시 넣어주고 싶으면
    formData.set("price", String(price));

    // 4. ✅ 이미지 파일을 FormData에 추가 (서버의 요구 필드명에 맞게)
    // 서버가 멀티파트 요청으로 파일과 텍스트를 함께 받습니다.

    // 4-A. 대표 이미지 추가 (단일 파일)
    // 서버에서 mainImageFile 이라는 필드명으로 받는다고 가정
    formData.append("mainImageFile", mainFile);

    // 4-B. 상세 이미지 추가 (다중 파일)
    // 서버에서 detailImageFiles 이라는 배열 필드명으로 받는다고 가정
    detailFiles.forEach(file => {
        formData.append("detailImageFiles", file);
    });

    // 5. 서버 통신 (API_BASE_URL + "/additem")
    try {
        const response = await fetch(API_BASE_URL + "/additem", {
            method: "POST",
            // 멀티파트 폼 데이터는 Content-Type 헤더를 명시적으로 설정하지 않습니다.
            // 브라우저가 자동으로 'multipart/form-data'와 경계를 설정해 줍니다.
            body: formData
        });

        if (response.status === 201 || response.ok) {
            alert(`상품 '${itemName}' 등록 완료!`);
            closeModal("new-product-modal");
            document.getElementById('new-product-form')?.reset();

            newProductImages.mainFile = null;
            newProductImages.detailFiles = [];

            renderNewProductImages();
            renderProductList(); // 상품 목록 갱신

        } else {
            // 서버에서 에러 메시지를 JSON으로 보낼 경우 처리
            const errorText = await response.text();
            console.error("서버 응답 오류:", errorText);
            alert(`상품 등록 실패! 서버 오류: ${response.status} ${errorText.substring(0, 50)}...`);
        }
    } catch (error) {
        console.error("등록 통신 오류:", error);
        alert("상품 등록 중 네트워크 오류가 발생했습니다.");
    }
}

function renderNewProductImages() {
    const mainArea = document.getElementById('new-main-image-area');
    const detailArea = document.getElementById('new-detail-image-area');
    if (!mainArea || !detailArea) {
        console.error("Image areas for new product modal not found.");
        return;
    }

    // 대표 이미지 영역 초기화 및 컨테이너 설정
    mainArea.innerHTML = '';
    const mainContentContainer = document.createElement('div');
    mainContentContainer.style.display = 'flex';
    mainContentContainer.style.gap = '30px';
    mainContentContainer.style.alignItems = 'flex-start';
    mainArea.appendChild(mainContentContainer);

    // ===== 1) 대표 이미지 영역 =====
    if (newProductImages.mainFile) {
        // A) 파일이 선택된 경우 (미리보기)
        const wrap = document.createElement('div');
        wrap.className = 'current-main-image-container';
        wrap.style.position = 'relative';

        const file = newProductImages.mainFile;

        // 이미지 아래 버튼 영역 (미리 정의)
        const btnArea = document.createElement('div');
        btnArea.className = 'action-buttons';

        const clearBtn = document.createElement('button');
        clearBtn.type = 'button';
        clearBtn.textContent = '선택 취소';
        clearBtn.className = 'btn-delete btn-small';
        clearBtn.onclick = () => {
            newProductImages.mainFile = null;
            renderNewProductImages();
        };

        btnArea.appendChild(clearBtn);

        // ⭐ 수정: FileReader 로직을 수정하여 썸네일을 정확히 삽입합니다.
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            const thumbContainer = document.createElement('div');
            thumbContainer.className = 'thumb';

            // 🚀 [최종 해결] btnArea 노드 앞에 thumbContainer를 삽입합니다.
            // 786번째 줄의 `wrap.prepend(thumbContainer);`를 이 코드로 대체하세요.
            wrap.insertBefore(thumbContainer, btnArea);

            thumbContainer.appendChild(img);
        };
        reader.readAsDataURL(file);

        // btnArea는 이미 정의되어 있으므로, wrap에 추가합니다.
        wrap.appendChild(btnArea); // 버튼 영역 추가

        mainContentContainer.appendChild(wrap);

    } else {
        // B) 파일이 없는 경우 (추가 버튼)
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.style.display = 'none';

        const label = document.createElement('label');
        label.className = 'image-upload-placeholder';
        label.innerHTML = `<i class="fa fa-camera"></i><br>대표 이미지 추가`;
        label.onclick = () => { input.click(); };
        label.style.width = '100%';
        label.style.height = '100%';
        label.style.border = '1px dashed #adb5bd';
        label.style.backgroundColor = 'transparent';
        label.style.color = 'var(--text-secondary)';
        label.style.fontSize = '12px';

        input.onchange = (e) => {
            const file = e.target.files[0];
            if (!file) return;
            newProductImages.mainFile = file;
            renderNewProductImages();
        };

        mainContentContainer.appendChild(label);
        mainContentContainer.appendChild(input);
    }

    // ===== 2) 상세 이미지 영역 =====
    detailArea.innerHTML = '';
    detailArea.style.display = 'flex';
    detailArea.style.flexWrap = 'wrap';

    // 새로 추가된 상세 이미지 리스트 렌더
    newProductImages.detailFiles.forEach((file, idx) => {
        const wrap = document.createElement('div');
        wrap.className = 'image-box';
        wrap.style.position = 'relative';

        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            const thumbContainer = document.createElement('div');
            thumbContainer.className = 'thumb';
            wrap.appendChild(thumbContainer); // 썸네일을 바로 추가 (이전 수정 반영)
            thumbContainer.appendChild(img);
        };
        reader.readAsDataURL(file);

        // 삭제 오버레이 사용
        const delOverlay = document.createElement('div');
        delOverlay.className = 'delete-button-overlay';

        const delBtn = document.createElement('button');
        delBtn.type = 'button';
        delBtn.textContent = 'X';
        delBtn.onclick = () => {
            newProductImages.detailFiles.splice(idx, 1);
            renderNewProductImages();
        };
        delOverlay.appendChild(delBtn);

        wrap.appendChild(delOverlay);
        detailArea.appendChild(wrap);
    });

    // ===== 3) 새 상세 이미지 추가 input (최대 5장) =====
    const currentDetailCount = newProductImages.detailFiles.length;

    if (currentDetailCount < MAX_DETAIL) { // MAX_DETAIL이 전역에 선언되었다고 가정합니다.

        const wrap = document.createElement('div');
        wrap.className = 'image-box';
        wrap.style.marginRight = '15px';
        wrap.style.marginBottom = '15px';
        wrap.style.padding = '0';

        const addInput = document.createElement('input');
        addInput.type = 'file';
        addInput.accept = 'image/*';
        addInput.multiple = true;
        addInput.style.display = 'none';

        const label = document.createElement('label');
        label.className = 'image-upload-placeholder';
        label.innerHTML = `<i class="fa fa-plus"></i><br>파일 추가 (${currentDetailCount}/${MAX_DETAIL})`;
        label.onclick = () => { addInput.click(); };

        label.style.width = '100%';
        label.style.height = '100%';
        label.style.border = 'none';
        label.style.backgroundColor = 'transparent';
        label.style.color = 'var(--text-secondary)';
        label.style.fontSize = '12px';

        addInput.onchange = (e) => {
            const files = Array.from(e.target.files || []);
            const allowance = MAX_DETAIL - currentDetailCount;
            const toAdd = files.slice(0, allowance);
            newProductImages.detailFiles.push(...toAdd);
            renderNewProductImages();
        };

        wrap.appendChild(label);
        wrap.appendChild(addInput);
        detailArea.appendChild(wrap);
    }
}

// ======================================
// 8. 수정(Update) 및 삭제(Delete)
// ======================================

async function populateEditForm(modalId, itemId) {
    if (modalId !== 'edit-product-modal') return;

    try {
        // 0) 상태 초기화
        editProductImages.reset();

        // 1) 상품 기본 정보 + 이미지 정보 병렬로 가져오기
        const [itemRes, imgRes] = await Promise.all([
            fetch(API_BASE_URL + '/item/' + itemId),
            fetch(API_BASE_URL + '/item/' + itemId + '/images')
        ]);

        if (!itemRes.ok) {
            throw new Error('상세 상품 데이터를 찾을 수 없습니다.');
        }

        const item = await itemRes.json();

        document.getElementById('edit-item-id').value = item.itemId;
        document.getElementById('edit-product-id-display').textContent = item.itemId;
        document.getElementById('edit-item-name').value = item.itemName || '';
        document.getElementById('edit-item-price').value = item.price || 0;

        const storIdEl = document.getElementById('edit-stor-id');
        if (storIdEl) {
            storIdEl.value = item.storId || '';
            storIdEl.disabled = true;
        }

        // 2) 이미지 리스트 세팅
        if (imgRes.ok) {
            const images = await imgRes.json(); // [{imageId, imageUrl, imageType}, ...]

            images.forEach(img => {
                if (img.imageType === 'MAIN') {
                    editProductImages.existingMain = img;
                } else if (img.imageType === 'DETAIL') {
                    editProductImages.existingDetails.push(img);
                }
            });
        } else {
            console.warn('이미지 목록을 불러오지 못했습니다.');
        }

        // 3) ✅ 실제 DOM에 그리기
        renderEditProductImages();

    } catch (error) {
        console.error('데이터 로드 오류:', error);
        alert('수정할 상품 데이터를 불러오는 데 실패했습니다.');
    }
}


function renderEditProductImages() {
    const mainArea = document.getElementById('edit-main-image-area');
    const detailArea = document.getElementById('edit-detail-image-area');
    if (!mainArea || !detailArea) return;

    // ===== 1) 대표 이미지 영역 =====
    mainArea.innerHTML = '';

    // 메인 영역 전체를 담는 컨테이너 생성 (이것이 이전에 mainImageWrapper 역할을 대체함)
    const mainContentContainer = document.createElement('div');
    mainContentContainer.style.display = 'flex';
    mainContentContainer.style.gap = '30px';
    mainContentContainer.style.alignItems = 'flex-start'; // 상단 정렬
    mainArea.appendChild(mainContentContainer);


    // 1-A) 기존 대표 이미지가 있고 삭제되지 않은 경우 (URL 사용)
    if (editProductImages.existingMain &&
        !editProductImages.deleteIds.has(editProductImages.existingMain.imageId)) {

        const img = editProductImages.existingMain;

        const wrap = document.createElement('div');
        wrap.className = 'current-main-image-container'; // CSS 3번 항목에서 추가했던 클래스 사용

        const imgEl = document.createElement('img');
        const cleanedUrl = img.imageUrl.replace(/^\/|\/$/g, '');
        imgEl.src = `/files/${cleanedUrl}`;

        const thumbContainer = document.createElement('div');
        thumbContainer.className = 'thumb';
        thumbContainer.appendChild(imgEl);

        // 이미지 아래 버튼 영역
        const btnArea = document.createElement('div');
        btnArea.className = 'action-buttons'; // CSS에서 정의된 버튼 영역 클래스

        const delBtn = document.createElement('button');
        delBtn.type = 'button';
        delBtn.textContent = '삭제';
        delBtn.className = 'btn-delete btn-small';
        delBtn.onclick = () => {
            editProductImages.deleteIds.add(editProductImages.existingMain.imageId);
            editProductImages.existingMain = null;
            renderEditProductImages();
        };
        btnArea.appendChild(delBtn);

        const changeInput = document.createElement('input');
        changeInput.type = 'file';
        changeInput.accept = 'image/*';
        changeInput.style.display = 'none'; // 숨김

        const changeBtn = document.createElement('button');
        changeBtn.type = 'button';
        changeBtn.textContent = '수정';
        changeBtn.className = 'btn-secondary btn-small';
        changeBtn.onclick = () => { changeInput.click(); };
        btnArea.appendChild(changeBtn); // 버튼 영역에 추가

        changeInput.onchange = (e) => {
            const file = e.target.files[0];
            if (!file) return;

            if (editProductImages.existingMain) {
                editProductImages.deleteIds.add(editProductImages.existingMain.imageId);
                editProductImages.existingMain = null;
            }
            editProductImages.newMainFile = file;
            renderEditProductImages();
        };

        // 최종 DOM 추가
        wrap.appendChild(thumbContainer);
        wrap.appendChild(btnArea);
        wrap.appendChild(changeInput); // input 태그는 숨겨져 있으므로 DOM에 추가합니다.
        mainContentContainer.appendChild(wrap); // ✅ 수정: mainImageWrapper 대신 mainContentContainer 사용

    } else if (editProductImages.newMainFile) {
        // 1-B) 새로 업로드할 파일이 선택된 경우 (미리보기)

        const wrap = document.createElement('div');
        wrap.className = 'current-main-image-container'; // 동일 클래스 사용
        wrap.style.position = 'relative';

        const file = editProductImages.newMainFile;

        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            const thumbContainer = document.createElement('div');
            thumbContainer.className = 'thumb';
            thumbContainer.appendChild(img);
            wrap.insertBefore(thumbContainer, wrap.firstChild); // 썸네일 추가
        };
        reader.readAsDataURL(file);

        // 이미지 아래 버튼 영역
        const btnArea = document.createElement('div');
        btnArea.className = 'action-buttons';

        const clearBtn = document.createElement('button');
        clearBtn.type = 'button';
        clearBtn.textContent = '선택 취소';
        clearBtn.className = 'btn-delete btn-small';
        clearBtn.onclick = () => {
            editProductImages.newMainFile = null;
            renderEditProductImages();
        };

        btnArea.appendChild(clearBtn);

        wrap.appendChild(btnArea); // 버튼 영역 추가
        mainContentContainer.appendChild(wrap); // ✅ 수정: mainImageWrapper 대신 mainContentContainer 사용

    } else {
        // 1-C) 대표 이미지가 없는 경우 (추가 버튼)

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.style.display = 'none';

        const label = document.createElement('label');
        label.className = 'image-upload-placeholder';
        label.innerHTML = `<i class="fa fa-camera"></i><br>대표 이미지 추가`;
        label.onclick = () => { input.click(); }

        label.style.width = '100%';
        label.style.height = '100%';
        label.style.border = '1px dashed #adb5bd'; // 🛠️ [핵심 수정 2] dashed border를 다시 적용
        label.style.backgroundColor = 'transparent';
        label.style.color = 'var(--text-secondary)';
        label.style.fontSize = '12px'; // 🛠️ [핵심 수정 2] 폰트 사이즈 재확인

        input.onchange = (e) => {
            const file = e.target.files[0];
            if (!file) return;
            editProductImages.newMainFile = file;
            renderEditProductImages();
        };

        mainContentContainer.appendChild(label); // ✅ 수정: mainImageWrapper 대신 mainContentContainer 사용
        mainContentContainer.appendChild(input);
    }

    // ... (2) 상세 이미지 렌더링 로직은 이전과 동일하게 유지)
    const aliveDetails = editProductImages.existingDetails.filter(
        (img) => !editProductImages.deleteIds.has(img.imageId)
    );

    const currentDetailCount = aliveDetails.length + editProductImages.newDetailFiles.length;

    // ===== 2) 상세 이미지 영역 =====
    detailArea.innerHTML = '';
    detailArea.style.display = 'flex';
    detailArea.style.flexWrap = 'wrap';

    // 2-A) 기존 상세 이미지 렌더 (삭제되지 않은 것만)
    editProductImages.existingDetails
        .filter(img => !editProductImages.deleteIds.has(img.imageId))
        .forEach(img => {
            const wrap = document.createElement('div');
            wrap.className = 'image-box';
            wrap.style.position = 'relative';

            const imgEl = document.createElement('img');
            const cleanedUrl = img.imageUrl.replace(/^\/|\/$/g, '');
            imgEl.src = `/files/${cleanedUrl}`;

            const thumbContainer = document.createElement('div');
            thumbContainer.className = 'thumb';
            thumbContainer.appendChild(imgEl);

            // 삭제 오버레이
            const delOverlay = document.createElement('div');
            delOverlay.className = 'delete-button-overlay';

            const delBtn = document.createElement('button');
            delBtn.type = 'button';
            delBtn.textContent = 'X';
            delBtn.onclick = () => {
                editProductImages.deleteIds.add(img.imageId);
                renderEditProductImages();
            };
            delOverlay.appendChild(delBtn);

            wrap.appendChild(thumbContainer);
            wrap.appendChild(delOverlay);
            detailArea.appendChild(wrap);
        });

    // 2-B) 새로 추가된 상세 이미지 리스트 렌더
    editProductImages.newDetailFiles.forEach((file, idx) => {
        const wrap = document.createElement('div');
        wrap.className = 'image-box';
        wrap.style.position = 'relative';

        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            const thumbContainer = document.createElement('div');
            thumbContainer.className = 'thumb';

            wrap.appendChild(thumbContainer);
            thumbContainer.appendChild(img);
        };
        reader.readAsDataURL(file);

        // 삭제 오버레이
        const delOverlay = document.createElement('div');
        delOverlay.className = 'delete-button-overlay';

        const delBtn = document.createElement('button');
        delBtn.type = 'button';
        delBtn.textContent = 'X';
        delBtn.onclick = () => {
            editProductImages.newDetailFiles.splice(idx, 1);
            renderEditProductImages();
        };
        delOverlay.appendChild(delBtn);

        wrap.appendChild(delOverlay);
        detailArea.appendChild(wrap);
    });

    // ===== 3) 새 상세 이미지 추가 input (최대 5장) =====
    if (currentDetailCount < 5) {

        const wrap = document.createElement('div');
        wrap.className = 'image-box';
        wrap.style.marginRight = '15px';
        wrap.style.marginBottom = '15px';
        wrap.style.padding = '0';

        const addInput = document.createElement('input');
        addInput.type = 'file';
        addInput.accept = 'image/*';
        addInput.multiple = true;
        addInput.style.display = 'none';

        const label = document.createElement('label');
        label.className = 'image-upload-placeholder';
        label.innerHTML = `<i class="fa fa-plus"></i><br>파일 추가 (${currentDetailCount}/5)`;
        label.onclick = () => { addInput.click(); };

        label.style.width = '100%';
        label.style.height = '100%';
        label.style.border = 'none';
        label.style.backgroundColor = 'transparent';
        label.style.color = 'var(--text-secondary)';
        label.style.fontSize = '12px';

        addInput.onchange = (e) => {
            const files = Array.from(e.target.files || []);
            const allowance = 5 - currentDetailCount;
            const toAdd = files.slice(0, allowance);
            editProductImages.newDetailFiles.push(...toAdd);
            renderEditProductImages();
        };

        wrap.appendChild(label);
        wrap.appendChild(addInput);
        detailArea.appendChild(wrap);
    }

    // 디버그용 로그
    console.log('[EDIT IMG] main =', editProductImages.existingMain,
        'details =', aliveDetails,
        'newDetails =', editProductImages.newDetailFiles);
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

    const formData = new FormData();
    formData.append('itemName', document.getElementById('edit-item-name').value);
    formData.append('price', document.getElementById('edit-item-price').value);
    formData.append('storId', document.getElementById('edit-stor-id').value);

    // ✅ 삭제할 이미지 id 리스트 (JSON 문자열로)
    const deleteIdsArray = Array.from(editProductImages.deleteIds);
    formData.append('deleteImageIds', JSON.stringify(deleteIdsArray));

    // ✅ 새 대표 이미지
    if (editProductImages.newMainFile) {
        formData.append('newMainImage', editProductImages.newMainFile);
    }

    // ✅ 새 상세 이미지들
    editProductImages.newDetailFiles.forEach(file => {
        formData.append('newDetailImages', file);
    });

    try {
        const response = await fetch(API_BASE_URL + '/item/' + itemId, {
            method: 'POST', // 또는 PUT, 백엔드 정의에 맞춰서
            body: formData   // ❗ Content-Type은 브라우저가 자동 설정
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

// async function handleDeleteProduct(itemId) {
//     if (!confirm(`상품 ID: ${itemId}을(를) 정말 삭제하시겠습니까?`)) {
//         return;
//     }
//
//     try {
//         const response = await fetch(API_BASE_URL + '/item/' + itemId, {
//             method: 'DELETE'
//         });
//
//         if (response.status === 204) {
//             alert(`상품 ID: ${itemId} 삭제 완료.`);
//             renderProductList();
//         } else {
//             alert('상품 삭제에 실패했습니다. (서버 오류)');
//         }
//     } catch (error) {
//         console.error('삭제 통신 오류:', error);
//         alert('상품 삭제 중 통신 오류가 발생했습니다.');
//     }
// }

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

// 이미지 미리보기 유틸리티 함수
function showImagePreview(inputElement, previewContainerId) {
    const previewContainer = document.getElementById(previewContainerId);
    if (!previewContainer) return;

    // 상세 이미지(multiple=true)는 기존 목록에 추가될 수 있지만,
    // 새 상품 등록 시에는 파일 선택할 때마다 기존 파일을 초기화합니다.
    previewContainer.innerHTML = '';

    const files = inputElement.files;
    if (files.length === 0) return;

    // 파일 목록을 순회하며 미리보기를 생성
    Array.from(files).forEach(file => {
        if (!file.type.startsWith('image/')) return;

        const reader = new FileReader();

        reader.onload = function(e) {
            const wrap = document.createElement('div');
            // CSS에서 정의된 스타일 클래스 사용
            wrap.className = 'image-box preview-box';

            const img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'thumb preview-thumb';

            wrap.appendChild(img);

            previewContainer.appendChild(wrap);
        };

        reader.readAsDataURL(file);
    });
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

    // 상세 이미지 5장 제한
    const detailInput = document.getElementById('detail-images');
    if (detailInput) {
        detailInput.addEventListener('change', (e) => {
            const files = Array.from(e.target.files);

            if (files.length > 5) {
                alert('상세 이미지는 최대 5장까지만 업로드할 수 있습니다.\n선택한 이미지 중 앞의 5장만 사용합니다.');

                // 앞의 5개만 남기고 나머지는 버림
                const dt = new DataTransfer();
                files.slice(0, 5).forEach(file => dt.items.add(file));
                e.target.files = dt.files;
            }
        });
    }

    const newMainImageInput = document.getElementById('main-image');
    if (newMainImageInput) {
        newMainImageInput.addEventListener('change', (e) => {
            // HTML에 추가된 <div id="new-main-preview-area"> 에 미리보기 표시
            showImagePreview(e.target, 'new-main-preview-area');
        });
    }

    const newDetailImagesInput = document.getElementById('detail-images');
    if (newDetailImagesInput) {
        newDetailImagesInput.addEventListener('change', (e) => {
            // HTML에 추가된 <div id="new-detail-preview-area"> 에 미리보기 표시
            showImagePreview(e.target, 'new-detail-preview-area');
        });
    }

    document.getElementById('new-farm-form')?.addEventListener('submit', handleNewFarm);
    document.getElementById('new-crop-form')?.addEventListener('submit', handleNewCrop);
    document.getElementById('new-product-form')?.addEventListener('submit', handleNewProduct);

    document.getElementById('edit-farm-form')?.addEventListener('submit', handleEditFarmAddress);
    document.getElementById('edit-crop-form')?.addEventListener('submit', handleEditCrop);
    document.getElementById('edit-product-form')?.addEventListener('submit', handleEditProduct);

    // 검색 버튼에 이벤트 리스너 추가
    document.querySelector('.filter-area .btn-secondary')?.addEventListener('click', () => {
        renderOrderList();
    });
});

document.querySelectorAll('.close-button').forEach(btn => {
    btn.addEventListener('click', (e) => {
        const modal = e.target.closest('.modal');
        if (modal) {
            closeModal(modal.id);
        }
    });
});
