// script.js
// ======================================
// 1. API 기본 설정 및 전역 상수
// ======================================
const API_BASE_URL = '/admin/shop';

// DB 구조에 맞게 STATUS(0/1) 및 STOR_ID 반영
let products = [
];
let orders = [
    { id: 'ORD-001', customer: '김고객', date: '2025-11-05', total: '24,000원', status: 'ready', products: [{ name: '유기농 방울토마토', qty: 2, price: 12000 }] },
    { id: 'ORD-002', customer: '이고객', date: '2025-11-06', total: '50,000원', status: 'paid', products: [{ name: '신선한 상추', qty: 10, price: 5000 }] }
];


// ======================================
// 🌟 2. 초기 로드 및 공통 기능
// ======================================

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


        });
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}


// ======================================
// ======================================

function renderAllLists() {
    renderProductList();
    renderOrderList();
}

}

    const list = document.getElementById('crop-list');
    if (!list) return;

            <td>
                <label class="switch">
                    <span class="slider"></span>
                </label>
            </td>
            <td>
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


// ======================================
// ======================================

function handleNewFarm(e) {
    e.preventDefault();
    closeModal('new-farm-modal');
}

    e.preventDefault();


    };

    try {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });

            closeModal('new-product-modal');
            renderProductList();
}


// ======================================
// ======================================

function handleEditFarm(e) {
    e.preventDefault();
    closeModal('edit-farm-modal');
}

function handleEditCrop(e) {
    e.preventDefault();
    closeModal('edit-crop-modal');
}

    e.preventDefault();

    const newName = document.getElementById('edit-item-name').value;


            closeModal('edit-product-modal');
        }


    try {

            renderProductList();
        }

        return;
    }
    }






    }



            }

    }

        e.preventDefault();

            return;
        }

        }