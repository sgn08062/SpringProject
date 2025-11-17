// /js/address.js (불필요 부분 제거 후 최소 버전)

const $ = (sel, el=document) => el.querySelector(sel);

const state = {
    list: []
};

// 초기 진입
document.addEventListener("DOMContentLoaded", async () => {
    await fetchList();
    render();
});

async function fetchList() {
    try {
        const res = await fetch("/user/api/address/", {
            headers: { "Accept": "application/json" }
            // CORS면 credentials: "include" + 서버 allowCredentials 필요
        });

        if (res.status === 401) {
            location.href = "/login";
            return;
        }
        if (!res.ok) throw new Error("목록 조회 실패");

        /** 기대 응답:
         * [
         *  { addressId, userId, address, addressName, recipientName, recipientPhone }, ...
         * ]
         */
        const data = await res.json();
        state.list = Array.isArray(data) ? data : [];
    } catch (e) {
        console.error(e);
        state.list = [];
    }
}

function render() {
    const container = $("#list");
    if (!state.list.length) {
        container.innerHTML = `
      <div class="auth-footer" style="margin-top:14px">
        저장된 주소가 없습니다.
      </div>`;
        return;
    }
    container.innerHTML = state.list.map(toCardHTML).join("");
}

function toCardHTML(a) {
    // 서버 응답 필드명에 맞춰 렌더
    const id = Number(a.addressId);
    const alias = escapeHTML(a.addressName ?? "주소");
    const name = escapeHTML(a.recipientName ?? "-");
    const phone = escapeHTML(a.recipientPhone ?? "-");
    const addr = escapeHTML(a.address ?? "");

    return `
    <article class="card" data-id="${id}">
    <!-- ✨ 보이지 않는 addressId (후속 로직에서 fallback 용) -->
    <input type="hidden" class="address-id" value="${id}">
      <div class="dot"></div>
      <div class="body">
        <div class="row">
          <div class="name">${name}</div>
          <span class="badge">${alias}</span>
          <button class="badge-btn" data-action="edit" data-id="${id}">
            수정
          </button>
          <button class="badge-btn danger" data-action="delete" data-id="${id}" type="button">
            삭제
          </button>
        </div>
        <div class="addr">${addr}</div>
        <div class="meta">연락처 ${phone}</div>
      </div>
    </article>
  `;
}

function escapeHTML(s){
    return String(s ?? "").replace(/[&<>"']/g, m => ({
        "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"
    }[m]));
}

// --- 모달 열기/닫기 (모달만 동작) ---
// list.html 에 연결된 /js/address.js 내에 넣기
document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("addrModal");
    const btnAdd = document.getElementById("btnAdd");
    const btnCancel = document.getElementById("btnCancel");
    const form = document.getElementById("addrForm");
    const title = document.getElementById("modalTitle");

    // ▶ 모달 열기: "+ 새 주소 추가"
    if (btnAdd && modal) {
        btnAdd.addEventListener("click", () => {
            if (form) {
                form.reset();
                if (form.addressId) form.addressId.value = ""; // ★ 신규 모드
            }
            if (title) title.textContent = "새 주소 추가";
            modal.showModal();
        });
    }


    // ▶ 모달 닫기: 취소 버튼
    if (btnCancel && modal) {
        btnCancel.addEventListener("click", () => modal.close());
    }

    // ▶ 모달 닫기: 배경 클릭
    if (modal) {
        modal.addEventListener("click", (e) => {
            if (e.target === modal) modal.close();
        });
    }

    // ▶ 저장(제출): address1+address2 → address 로 합쳐서 JSON 전송
    if (form) {
        form.addEventListener("submit", onSubmitAddress);
    }

    // ▼ 카드 내부 버튼 클릭 위임(수정 버튼 등)
    const listEl = document.getElementById("list");
    if (listEl) {
        listEl.addEventListener("click", onListAction);
    }

});

async function onSubmitAddress(e) {
    e.preventDefault();
    const f = e.currentTarget;

    const address1 = f.address1.value.trim();
    const address2 = f.address2.value.trim();
    const address = [address1, address2].filter(Boolean).join(", ");

    // const payload = {
    //     recipientName: f.recipientName.value.trim(),
    //     recipientPhone: f.recipientPhone.value.trim(),
    //     address: address,
    //     addressName: f.addressName?.value?.trim()
    // };
    //
    // try {
    //     const res = await fetch("/user/api/address/insert", {
    //         method: "POST",
    //         headers: { "Content-Type": "application/json", "Accept": "text/plain" },
    //         // CORS 환경이면 필요: credentials: "include"
    //         body: JSON.stringify(payload)
    //     });
    //
    //     if (res.status === 401) { location.href = "/login"; return; }
    //
    //     const text = await res.text();
    //     if (text === "success") {
    //         document.getElementById("addrModal").close();
    //         await fetchList();  // ▶ 목록 재조회
    //         render();           // ▶ 렌더링
    //         safeToast("주소가 추가되었습니다.");     // 성공 토스트
    //     } else {
    //         safeToast("주소 추가 실패");           // 실패 토스트
    //         // 모달은 열어둔 상태로 사용자가 수정 가능
    //     }
    // } catch (err) {
    //     console.error(err);
    //     safeToast("네트워크 오류가 발생했습니다."); // 오류 토스트
    // }
    const payload = {
        recipientName: f.recipientName.value.trim(),
        recipientPhone: f.recipientPhone.value.trim(),
        address: address,
        addressName: f.addressName?.value?.trim()
    };

    const isEdit = !!f.addressId?.value;
    if (isEdit) {
        // ★ 서버가 addressId를 JSON에서 받도록 변경됨
        payload.addressId = Number(f.addressId.value);
    }
    const url    = isEdit
        ? `/user/api/address/update`              // ★ 변경: POST /update
        : `/user/api/address/insert`;
    const method = "POST";                      // ★ 변경: 항상 POST

    try {
        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json", "Accept": "text/plain" },
            body: JSON.stringify(payload)
        });

        if (res.status === 401) { location.href = "/login"; return; }

        const text = await res.text();
        if (text === "success") {
            document.getElementById("addrModal").close();
            await fetchList();
            render();
            safeToast(isEdit ? "주소가 수정되었습니다." : "주소가 추가되었습니다.");
            // 폼/상태 초기화
            f.reset();
            if (f.addressId) f.addressId.value = "";
        } else {
            safeToast(isEdit ? "주소 수정 실패" : "주소 추가 실패");
        }
    } catch (err) {
        console.error(err);
        safeToast("네트워크 오류가 발생했습니다.");
    }

}

// 안전 토스트: 전역 toast()가 있으면 그걸 쓰고, 없으면 임시 토스트 생성
function safeToast(message) {
    if (typeof window.toast === "function") {
        window.toast(message);
        return;
    }
    // 임시 토스트
    let box = document.getElementById("__toast_box__");
    if (!box) {
        box = document.createElement("div");
        box.id = "__toast_box__";
        Object.assign(box.style, {
            position: "fixed", left: "50%", bottom: "24px", transform: "translateX(-50%)",
            display: "flex", flexDirection: "column", gap: "8px", zIndex: 9999
        });
        document.body.appendChild(box);
    }
    const toast = document.createElement("div");
    toast.textContent = message;
    Object.assign(toast.style, {
        background: "#2E7D32", color: "#fff", padding: "10px 14px",
        borderRadius: "10px", boxShadow: "0 10px 30px rgba(0,0,0,.15)",
        fontWeight: 700, letterSpacing: ".2px", maxWidth: "80vw"
    });
    box.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = "opacity .25s ease";
        toast.style.opacity = "0";
        setTimeout(() => toast.remove(), 250);
    }, 1700);
}

function onListAction(e) {
    const btn = e.target.closest("button[data-action]");
    if (!btn) return;

    const action = btn.dataset.action;
    const card = btn.closest(".card");
    const id =
        Number(btn.dataset.id) ||
        Number(card?.dataset.id) ||
        Number(card?.querySelector(".address-id")?.value);
    if (action === "edit") {
        openEditModal(id); // ▼ 아래 함수에서 모달을 “수정 모드”로 오픈
    }
    else if (action === "delete") {
        onDeleteAddress(id, btn);
    }
}

function openEditModal(id) {
    const modal = document.getElementById("addrModal");
    const form  = document.getElementById("addrForm");
    const title = document.getElementById("modalTitle");
    if (!modal || !form) return;

    // 목록에서 대상 찾기
    const item = state.list.find(a => Number(a.addressId) === Number(id));
    if (!item) return;

    // 제목 변경: 수정 모드
    if (title) title.textContent = "주소 수정";

    // address를 address1/address2로 분해(첫 콤마 기준)
    const [address1, address2] = splitAddress(item.address ?? "");

    // 값 주입
    form.recipientName.value  = item.recipientName ?? "";
    form.recipientPhone.value = item.recipientPhone ?? "";
    form.address1.value       = address1;
    form.address2.value       = address2;
    form.addressName.value = item.addressName ?? "";
    // 수정 모드 표식을 위해 hidden 필드에 addressId 세팅
    if (form.addressId) form.addressId.value = item.addressId;

    modal.showModal();
}

function splitAddress(full) {
    const idx = full.indexOf(",");
    if (idx === -1) return [full.trim(), ""];
    return [full.slice(0, idx).trim(), full.slice(idx + 1).trim()];
}


async function onDeleteAddress(id, btnEl) {
    if (!confirm("이 주소를 삭제할까요?")) return;

    try {
        // 서버 규약에 맞춰 두 가지 중 택1
        // (A) 현재 컨벤션에 맞춤: POST /delete + JSON body
        const res = await fetch("/user/api/address/delete", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Accept": "text/plain" },
            body: JSON.stringify({ addressId: id })
        });

        // (B) RESTful하게 쓰고 싶다면:
        // const res = await fetch(`/user/api/address/${id}`, { method: "DELETE", headers: { "Accept": "text/plain" } });

        if (res.status === 401) { location.href = "/login"; return; }
        const text = await res.text();

        if (text === "success") {
            await fetchList();
            render();
            safeToast("주소가 삭제되었습니다.");
        } else {
            safeToast("주소 삭제 실패");
        }
    } catch (err) {
        console.error(err);
        safeToast("네트워크 오류가 발생했습니다.");
    }
}
