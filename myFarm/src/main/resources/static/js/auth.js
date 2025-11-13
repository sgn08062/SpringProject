// 가벼운 토스트
function toast(msg, type="info"){
    const t = document.createElement("div");
    t.textContent = msg;
    t.style.cssText = `
    position:fixed; left:50%; top:86px; transform:translateX(-50%);
    background:${type==="success"?"#2E7D32":"#FFFFFF"};
    color:${type==="success"?"#fff":"#1B1D19"};
    border:1px solid ${type==="success"?"#2E7D32":"#E4EFE6"};
    border-radius:999px; padding:10px 16px; font-weight:800; z-index:9999;
    box-shadow:0 8px 24px rgba(0,0,0,.12); opacity:0; transition:.22s ease;
  `;
    document.body.appendChild(t);
    requestAnimationFrame(()=>{ t.style.opacity="1"; t.style.top="96px"; });
    setTimeout(()=>{ t.style.opacity="0"; t.style.top="86px"; setTimeout(()=>t.remove(),200); }, 2000);
}

/*************************
 * 로그인: /account/login 페이지에서 동작
 *************************/
// ===== 로그인 =====(동작 확인 완료)
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const body = new URLSearchParams(new FormData(form));

        try {
            const res = await fetch('/api/account/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body,
                credentials: 'include'
            });

            const text = (await res.text()).trim();
            if (res.ok && text === 'success') {
                location.href = '/common/main';
            } else {
                toast('로그인 실패. 아이디/비밀번호를 확인하세요.');
            }
        } catch (err) {
            console.error(err);
            toast('로그인 중 오류가 발생했습니다.');
        }
    });
});

/*************************
 * 로그아웃: 아무 페이지에서나 사용 가능
 * <button id="logoutBtn"> 또는
 * <a href="#" data-action="logout">
 *************************/
// ===== 로그아웃 ===== (확인 완료)
(() => {
    // 타겟 수집: #logoutBtn 1개 + data-action="logout" 여러 개
    const targets = [];
    const btn = document.getElementById("logoutBtn");
    if (btn) targets.push(btn);
    document.querySelectorAll('[data-action="logout"]').forEach(el => targets.push(el));

    // 바인딩
    targets.forEach(el => {
        el.addEventListener("click", async (e) => {
            e.preventDefault();
            try {
                const res = await fetch("/api/account/logout", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                    body: "",                 // 바디 없음
                    credentials: "include"    // 세션 쿠키 포함
                });

                const text = (await res.text()).trim();

                if (res.ok && text === "success") {
                    // 성공: 메인 페이지로 이동
                    location.href = "/account/login";
                } else {
                    // 실패: 이동하지 않고 토스트만
                    toast("로그아웃 실패. 잠시 후 다시 시도하세요.");
                }
            } catch (err) {
                console.error(err);
                location.href = "/account/login";
            }
        });
    });
})();


// ===== 회원가입 - 아이디 중복확인 & 제출 버튼 제어 =====
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");
    const loginIdInput = document.getElementById("loginId");
    const idCheckBtn = document.getElementById("idCheckBtn");
    const submitBtn = form.querySelector('button[type="submit"]');
    const loginIdMsg = document.querySelector('.field-msg[data-for="loginId"]');

    // 제출 버튼은 기본 비활성화
    submitBtn.disabled = true;

    // 상태 플래그: 아이디가 서버에서 'unique'로 확인되었는지
    let isLoginIdUnique = false;

    // 입력이 바뀌면 다시 검증 필요 → 제출 비활성화
    loginIdInput.addEventListener("input", () => {
        isLoginIdUnique = false;
        submitBtn.disabled = true;
        setFieldMsg(loginIdMsg, "아이디 중복 확인을 해주세요.", "warn");
    });

    // 중복 확인 버튼 클릭
    idCheckBtn.addEventListener("click", async () => {
        const loginId = (loginIdInput.value || "").trim();

        // 1) 형식 검증: 4~20자 영문/숫자
        const idPattern = /^[A-Za-z0-9]{4,20}$/;
        if (!idPattern.test(loginId)) {
            setFieldMsg(loginIdMsg, "아이디는 4~20자의 영문/숫자만 가능합니다.", "error");
            loginIdInput.focus();
            return;
        }

        try {
            // 2) 서버 중복 확인
            const body = new URLSearchParams({ loginId }).toString();
            const res = await fetch("/api/account/idcheck", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                body,
                credentials: "include",
            });

            const text = (await res.text()).trim(); // "dup" | "unique"

            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }

            if (text === "unique") {
                isLoginIdUnique = true;
                submitBtn.disabled = false;
                setFieldMsg(loginIdMsg, "사용 가능한 아이디입니다.", "success");
                safeToast("사용 가능한 아이디입니다.");
            } else if (text === "dup") {
                isLoginIdUnique = false;
                submitBtn.disabled = true;
                setFieldMsg(loginIdMsg, "이미 사용 중인 아이디입니다.", "error");
                safeToast("중복된 아이디입니다.");
            } else {
                // 예상치 못한 응답
                isLoginIdUnique = false;
                submitBtn.disabled = true;
                setFieldMsg(loginIdMsg, "중복 확인에 실패했습니다. 잠시 후 다시 시도하세요.", "error");
                safeToast("중복 확인 실패");
            }
        } catch (err) {
            console.error(err);
            isLoginIdUnique = false;
            submitBtn.disabled = true;
            setFieldMsg(loginIdMsg, "네트워크 오류가 발생했습니다. 잠시 후 다시 시도하세요.", "error");
            safeToast("네트워크 오류");
        }
    });

    // 폼 제출 시 안전장치 (혹시 사용자가 DOM 조작했을 경우 대비)
    form.addEventListener("submit", (e) => {
        if (!isLoginIdUnique) {
            e.preventDefault();
            setFieldMsg(loginIdMsg, "아이디 중복 확인을 먼저 해주세요.", "warn");
            safeToast("아이디 중복 확인을 먼저 해주세요.");
        }
    });

    // 유틸: 필드 메시지 표시
    function setFieldMsg(el, msg, type /* 'success' | 'error' | 'warn' */) {
        if (!el) return;
        el.textContent = msg || "";
        el.classList.remove("success", "error", "warn");
        if (type) el.classList.add(type);
    }

    // 유틸: toast()가 있으면 사용, 없으면 alert 대체
    function safeToast(message) {
        if (typeof toast === "function") {
            toast(message);
        } else {
            // 프로젝트에 toast 미구현 시 임시 대체
            // 필요 없으면 이 줄 삭제 가능
            console.log("[TOAST]", message);
        }
    }

    // 초기 안내
    setFieldMsg(loginIdMsg, "아이디 중복 확인을 해주세요.", "warn");
});


/*************************
 * 회원가입: /account/register 페이지에서 동작(선택)
 *************************/
// ===== 회원가입 =====(동작 확인 완료)
(() => {
    const form = document.getElementById("signupForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // 필드 참조
        const loginId = document.getElementById("loginId")?.value.trim() || "";
        const userPw  = document.getElementById("userPw")?.value || "";
        const userPw2 = document.getElementById("userPw2")?.value || "";
        const emailEl = document.getElementById("email");
        const phoneEl = document.getElementById("phone");

        // 간단 유효성 체크
        if (!loginId || !userPw || !userPw2) {
            toast("아이디/비밀번호를 입력하세요.");
            return;
        }
        if (userPw !== userPw2) {
            toast("비밀번호가 일치하지 않습니다.");
            return;
        }
        // HTML5 유효성(이메일/휴대폰)도 함께 확인
        if (emailEl && !emailEl.checkValidity()) {
            toast("올바른 이메일 형식을 입력하세요.");
            emailEl.focus();
            return;
        }
        if (phoneEl && !phoneEl.checkValidity()) {
            toast("휴대폰 번호 형식을 확인하세요.");
            phoneEl.focus();
            return;
        }

        // x-www-form-urlencoded 본문 생성
        const body = new URLSearchParams(new FormData(form));
        body.delete("userPw2"); // 서버에서 쓰지 않는 확인용 필드는 제거(선택)

        try {
            const res = await fetch("/api/account/register", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                body,
            });

            const text = (await res.text()).trim();

            if (res.ok && text === "success") {
                toast("회원가입 완료! 로그인 페이지로 이동합니다.", "success");
                setTimeout(() => (location.href = "/account/login"), 700);
            } else {
                toast("회원가입 실패. 입력값을 확인하세요.");
            }
        } catch (err) {
            console.error(err);
            toast("회원가입 중 오류가 발생했습니다.");
        }
    });
})();

