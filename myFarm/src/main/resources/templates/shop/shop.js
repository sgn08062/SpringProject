// shop.js

const API_BASE_URL = '/shop'; // ShopController의 @RequestMapping("/shop")과 일치

// ======================================
// 🌟 1. 초기 로드 및 공통 모달 기능
// ======================================

document.addEventListener('DOMContentLoaded', () => {
    renderProductList();

    // 등록/수정 폼 핸들러 연결
    document.getElementById('new-product-form')?.addEventListener('submit', handleNewProduct);
    document.getElementById('edit-product-form')?.addEventListener('submit', handleEditProduct);
});

// 모달 열기/닫기 함수 (HTML에서 onclick으로 호출)
function openModal(modalId, itemId = null) {
    document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
    const modal = document.getElementById(modalId);
    if (modal) {
        if (modalId === 'edit-product-modal' && itemId) {
            document.getElementById('edit-product-id-display').textContent = itemId;
            document.getElementById('edit-item-id').value = itemId;
            populateEditForm(itemId); // 데이터 채우기 호출
        }
        modal.style.display = 'block';
    }
}
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal ? modal.style.display = 'none' : null;
}


// ======================================
// 🌟 2. 상품 조회 및 렌더링 (GET /shop)
// ======================================

async function renderProductList() {
    const list = document.getElementById('product-list');
    list.innerHTML = '<tr><td colspan="5">데이터를 불러오는 중...</td></tr>';

    try {
        const response = await fetch(API_BASE_URL); // GET /shop
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const products = await response.json();

        if (products.length === 0) {
            list.innerHTML = '<tr><td colspan="5">등록된 상품이 없습니다.</td></tr>';
            return;
        }

        list.innerHTML = products.map(product => `
            <tr data-id="${product.itemId}">
                <td>${product.itemId}</td>
                <td>${product.itemName}</td>
                <td>${product.price ? product.price.toLocaleString() + '원' : 'N/A'}</td>
                <td>${product.storId || 'N/A'}</td>
                <td>
                    <button class="btn-small btn-edit" onclick="openModal('edit-product-modal', ${product.itemId})">수정</button> 
                    <button class="btn-small btn-delete" onclick="handleDeleteProduct(${product.itemId})">삭제</button>
                </td>
            </tr>
        `).join('');

        document.getElementById('summary-total-items').textContent = products.length + '개';

    } catch (error) {
        console.error('상품 목록 로딩 오류:', error);
        list.innerHTML = '<tr><td colspan="5">상품 목록을 불러오는 데 실패했습니다. 서버 상태를 확인하세요.</td></tr>';
    }
}


// ======================================
// 🌟 3. 상품 등록 (POST /shop/additem)
// ======================================

async function handleNewProduct(e) {
    e.preventDefault();

    const itemVO = {
        itemName: document.getElementById('new-item-name').value,
        price: parseInt(document.getElementById('new-item-price').value || 0),
        storId: document.getElementById('new-stor-id').value // String 타입으로 처리
    };

    try {
        const response = await fetch(API_BASE_URL + '/additem', { // POST /shop/additem
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(itemVO)
        });

        if (response.status === 201) {
            alert(`상품 '${itemVO.itemName}' 등록 완료!`);
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
// 🌟 4. 상품 수정 (PUT /shop/item/{id})
// ======================================

// [보조 함수] 수정 모달에 기존 데이터 채우기 (GET 요청을 통해 상세 데이터 가져옴)
async function populateEditForm(itemId) {
    try {
        // 상세조회 API가 없으므로, 현재 목록 전체를 다시 불러와 찾습니다. (비효율적이지만 현재 API 구조에 맞춤)
        const response = await fetch(API_BASE_URL);
        const products = await response.json();
        const item = products.find(p => p.itemId === itemId);

        if (item) {
            document.getElementById('edit-item-name').value = item.itemName || '';
            document.getElementById('edit-item-price').value = item.price || 0;
            document.getElementById('edit-stor-id').value = item.storId || '';
        } else {
            alert('수정할 상품 데이터를 찾을 수 없습니다.');
        }
    } catch (error) {
        console.error('데이터 로드 오류:', error);
    }
}

async function handleEditProduct(e) {
    e.preventDefault();

    const itemId = document.getElementById('edit-item-id').value;

    const itemVO = {
        itemName: document.getElementById('edit-item-name').value,
        price: parseInt(document.getElementById('edit-item-price').value || 0),
        storId: document.getElementById('edit-stor-id').value
    };

    try {
        const response = await fetch(API_BASE_URL + '/item/' + itemId, { // PUT /shop/item/{id}
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(itemVO)
        });

        if (response.ok) { // 200 OK
            alert(`상품 ID ${itemId} 정보 수정 완료!`);
            closeModal('edit-product-modal');
            renderProductList();
        } else {
            alert('상품 수정 실패! 서버 응답을 확인하세요.');
        }

    } catch (error) {
        console.error('수정 통신 오류:', error);
        alert('상품 수정 중 오류가 발생했습니다.');
    }
}


// ======================================
// 🌟 5. 상품 삭제 (DELETE /shop/item/{id})
// ======================================

async function handleDeleteProduct(itemId) {
    if (!confirm(`상품 ID: ${itemId}을(를) 정말 삭제하시겠습니까?`)) {
        return;
    }

    try {
        const response = await fetch(API_BASE_URL + '/item/' + itemId, { // DELETE /shop/item/{id}
            method: 'DELETE'
        });

        if (response.status === 204) { // 204 No Content
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