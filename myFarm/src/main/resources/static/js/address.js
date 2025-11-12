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
      <div class="dot"></div>
      <div class="body">
        <div class="row">
          <div class="name">${name}</div>
          <span class="badge">${alias}</span>
          <button class="badge-btn"
                  data-action="edit-alias"
                  data-id="${id}">수정</button>
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
            if (form) form.reset();
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
});

async function onSubmitAddress(e) {
    e.preventDefault();
    const f = e.currentTarget;

    const address1 = f.address1.value.trim();
    const address2 = f.address2.value.trim();
    const address = [address1, address2].filter(Boolean).join(", ");

    const payload = {
        recipientName: f.recipientName.value.trim(),
        recipientPhone: f.recipientPhone.value.trim(),
        address: address,
        addressName: f.addressName?.value?.trim()
    };

    try {
        const res = await fetch("/user/api/address/insert", {
            method: "POST",
            headers: { "Content-Type": "application/json", "Accept": "text/plain" },
            // CORS 환경이면 필요: credentials: "include"
            body: JSON.stringify(payload)
        });

        if (res.status === 401) { location.href = "/login"; return; }

        const text = await res.text();
        if (text === "success") {
            document.getElementById("addrModal").close();
            await fetchList();  // ▶ 목록 재조회
            render();           // ▶ 렌더링
            safeToast("주소가 추가되었습니다.");     // 성공 토스트
        } else {
            safeToast("주소 추가 실패");           // 실패 토스트
            // 모달은 열어둔 상태로 사용자가 수정 가능
        }
    } catch (err) {
        console.error(err);
        safeToast("네트워크 오류가 발생했습니다."); // 오류 토스트
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
