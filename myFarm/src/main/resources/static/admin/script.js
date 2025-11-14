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

    // ✅ 등록된 농가 정보(주소) 서버에서 불러오기
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

    // ✅ 기존 더미 렌더링 대신 서버 데이터로 교체
    fetchCrops()
        .then(cropList => {
            renderCropListFromData(cropList);
            // ✅ 목록 렌더 후 자동 갱신 시작
            startCropAutoRefresh(1000);   // 1초 간격 (원하면 2000/3000 조절)
            refreshCropProgressCells();   // 첫 동기화
        })
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
    document.getElementById('edit-farm-form')?.addEventListener('submit', handleEditFarmAddress);
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
    if(!modal) return;

    const idDisplay = document.getElementById(modalId.replace('modal', 'id-display'));
    if(idDisplay) idDisplay.textContent = itemId ?? '';

    // 수정 모달일 경우 서버에서 단건 조회로 채우기
    if (modalId === 'edit-crop-modal' && itemId != null) {
        loadCropIntoEditForm(itemId).catch(() => {
            alert('농작물 정보를 불러오지 못했습니다.');
        });
    }

    if (modalId === 'order-detail-modal' && itemId) {
        populateOrderDetailModal(itemId);
    }

    modal.style.display = 'block';
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
    //renderFarmList();
    //renderCropList();
    renderProductList();
    renderOrderList();
}
// ✅ 서버에서 농장 정보 요청
async function fetchAddress(){
    const res = await fetch('/admin/api/address/', {
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
    });
    if(!res.ok) throw new Error("주소 목록 로딩 실패");
    const list = await res.json();
    return await list;
}

// 테이블 렌더
function renderFarmAddressFromData(addr){
    const tbody = document.getElementById("farm-info");
    if(!tbody) return;

    if(!Array.isArray(addr) || addr.length === 0){
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


    // 셀에서 그대로 값 읽어오기
    const name    = tr.querySelector('.col-name')?.textContent?.trim()    || '';
    const address = tr.querySelector('.col-address')?.textContent?.trim() || '';
    const owner   = tr.querySelector('.col-owner')?.textContent?.trim()   || '';
    const phone   = tr.querySelector('.col-phone')?.textContent?.trim()   || '';

    // 모달에 세팅
    document.getElementById('edit-farm-id-display').textContent = String(addressId);
    document.getElementById('edit-farm-name').value     = name;
    document.getElementById('edit-farm-address').value  = address;
    document.getElementById('edit-farm-owner').value    = owner;
    document.getElementById('edit-farm-contact').value  = phone;

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
            credentials: 'include',                // 세션 쿠키 포함(중요)
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const text = await res.text().catch(()=>'');
        if (!res.ok || text !== 'success') throw new Error(text || '업데이트 실패');

        // 최신 목록 재조회 → 테이블 갱신
        const list = await fetchAddress();
        renderFarmAddressFromData(list);

        closeModal('edit-farm-modal');
        alert('주소 정보가 수정되었습니다.');
    } catch (err) {
        alert('수정 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
}


// ✅ 서버에서 농작물 목록 요청
async function fetchCrops() {
    const res = await fetch('/admin/api/crops', {
        headers: { 'Content-Type': 'application/json' }
    });
    if (!res.ok) throw new Error('Failed to load crops');
    const list = await res.json();

    return list;
}

// ✅ 퍼센트 계산
function percentOf(crop) {
    const et = Number(crop.elapsedTick);
    const gt = Number(crop.growthTime);
    if (!Number.isFinite(et) || !Number.isFinite(gt) || gt <= 0) return '-';
    return Math.min(100, Math.floor((et / gt) * 100)) + '%';
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
                <!-- ✅ 퍼센트 변경 함수 -->
                <td id="crop-progress-${crop.cropId}">${percentOf(crop)}</td>
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

// ✅ 재배상태 토글 스위치
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

// ✅ 상태(%)만 부분 갱신
async function refreshCropProgressCells() {
    try {
        const cropList = await fetchCrops(); // GET /admin/api/crops
        cropList.forEach(crop => {
            const cell = document.getElementById(`crop-progress-${crop.cropId}`);
            if (cell) cell.textContent = percentOf(crop);
        });
    } catch (e) {
        // 네트워크 오류시 무시 (다음 틱에 복구)
        // console.warn('progress refresh failed', e);
    }
}

async function loadCropIntoEditForm(cropId){
    const res = await fetch(`/admin/api/crops/${cropId}`);
    if(!res.ok) throw new Error('crop not found');
    crop = await res.json();

    document.getElementById('edit-crop-id-display').textContent = crop.cropId ?? '';
    document.getElementById('edit-crop-name').value   = crop.cropName ?? '';
    document.getElementById('edit-growth-time').value = (crop.growthTime ?? 60);
    document.getElementById('edit-quantity').value    = (crop.quantity ?? 0);
    document.getElementById('edit-unit-name').value   = crop.unitName ?? '';
    document.getElementById('edit-reg-date').value    = (crop.regDate ?? '').toString().slice(0,10); // yyyy-MM-dd
}

let cropProgressTimer = null;
function startCropAutoRefresh(intervalMs = 1000) {
    if (cropProgressTimer) clearInterval(cropProgressTimer);
    cropProgressTimer = setInterval(refreshCropProgressCells, intervalMs);
}

// 탭이 백그라운드로 가면 멈추고, 다시 오면 재개 (리소스 절약)
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        if (cropProgressTimer) { clearInterval(cropProgressTimer); cropProgressTimer = null; }
    } else {
        startCropAutoRefresh();
        // 복귀 시 한 번 즉시 동기화
        refreshCropProgressCells();
    }
});

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

// ✅ 새 농작물 등록: 백엔드와 연결
async function handleNewCrop(e) {
    e.preventDefault();

    const name       = document.getElementById('crop-name')?.value?.trim();
    const quantity   = parseInt(document.getElementById('quantity')?.value || '0', 10) || 0;
    const unitName   = document.getElementById('unit-name')?.value?.trim() || '';
    const regDate    = document.getElementById('reg-date')?.value || null; // ✅ 파종일 → regDate
    const statusSel  = document.getElementById('status')?.value;              // enable/disable
    const gtRaw      = document.getElementById('growth-time')?.value || '';
    let   growthTime = parseInt(gtRaw, 10);
    if (!Number.isFinite(growthTime) || growthTime <= 0) growthTime = 60;

    if (!name) { alert('농작물명을 입력하세요.'); return; }

    const payload = {
        cropName: name,
        quantity: quantity,
        unitName: unitName,
        regDate: regDate,                         // ✅ 서버 필드명 regDate로 보냄
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
        if (!res.ok) throw new Error(await res.text().catch(()=> '등록 실패'));

        const list = await fetchCrops();
        renderCropListFromData(list);
        closeModal('new-crop-modal');
        document.getElementById('new-crop-form')?.reset();
        alert('농작물이 등록되었습니다.');
    } catch (err) {
        alert('등록 중 오류가 발생했습니다.\n' + (err?.message || ''));
    }
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

function handleEditCrop(e) {
    e.preventDefault();
    const cropId = document.getElementById('edit-crop-id-display').textContent;

    const name     = document.getElementById('edit-crop-name')?.value?.trim();
    const gtRaw    = document.getElementById('edit-growth-time')?.value;
    const qtyRaw   = document.getElementById('edit-quantity')?.value;
    const unitName = document.getElementById('edit-unit-name')?.value?.trim();
    const regDate  = document.getElementById('edit-reg-date')?.value || null;

    const growthTime = gtRaw === ''  ? null : Number(gtRaw);
    const quantity   = qtyRaw === '' ? null : Number(qtyRaw);

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

// ✅ 공통 삭제 핸들러
async function handleDelete(type, id) {
    const label = (type === 'crop' ? '농작물' : (type === 'product' ? '상품' : '농가'));
    if (!confirm(`${label} ID: ${id}을(를) 정말 삭제하시겠습니까?`)) return;

    // 1) 농작물: 서버에 DELETE 요청
    if (type === 'crop') {
        try {
            const res = await fetch(`/admin/api/crops/${id}`, { method: 'DELETE' });
            if (!res.ok && res.status !== 204) {
                const msg = await res.text().catch(()=>'');
                throw new Error(msg || '삭제 실패');
            }

            // 즉시 DOM에서 한 줄 제거 (빠른 피드백)
            const tr = document.querySelector(`#crop-list tr[data-id="${id}"]`);
            if (tr) tr.remove();

            // 안전하게 서버 상태와 동기화
            const list = await fetchCrops();
            renderCropListFromData(list);

            alert('삭제되었습니다.');
        } catch (err) {
            alert('삭제 중 오류가 발생했습니다.\n' + (err?.message || ''));
        }
        return;
    }

    // 2) (기존) 더미 데이터 삭제 유지
    if (type === 'product') {
        products = products.filter(p => p.id !== id);
        renderProductList();
        alert('상품이 삭제되었습니다. (DB DELETE 필요)');
        return;
    }

    if (type === 'farm') {
        farms = farms.filter(f => f.id !== id);
        renderFarmList();
        alert('농가가 삭제되었습니다. (DB DELETE 필요)');
        return;
    }
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