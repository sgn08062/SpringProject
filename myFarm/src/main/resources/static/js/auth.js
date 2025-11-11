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

// 로컬 “유저 DB”
const loadUsers = () => JSON.parse(localStorage.getItem("userDatabase")||"[]");
const saveUsers = (db) => localStorage.setItem("userDatabase", JSON.stringify(db));

// ===== 회원가입 =====
(() => {
    const form = document.getElementById("signupForm");
    if (!form) return;

    const idInput = document.getElementById("signupId");
    const pwInput = document.getElementById("signupPw");
    const pw2Input = document.getElementById("signupPw2");
    const nameInput = document.getElementById("name");
    const emailId = document.getElementById("emailId");
    const emailDomain = document.getElementById("emailDomain");
    const phone = document.getElementById("phone");
    const addrMain = document.getElementById("addrMain");
    const addrDetail = document.getElementById("addrDetail");
    const idCheckBtn = document.getElementById("idCheckBtn");
    const addrSearchBtn = document.getElementById("addrSearchBtn");

    const setMsg = (key, text, type="error") => {
        const el = document.querySelector(`.field-msg[data-for="${key}"]`);
        if (!el) return;
        el.textContent = text;
        el.classList.remove("error","success");
        if (text) el.classList.add(type);
        else el.style.display="none";
    };

    let idChecked = false;

    idInput.addEventListener("input", () => { idChecked = false; setMsg("signupId",""); });

    idCheckBtn.addEventListener("click", () => {
        const id = idInput.value.trim();
        if (id.length < 6 || id.length > 20) {
            setMsg("signupId","아이디는 6~20자로 입력하세요.","error");
            return;
        }
        if (loadUsers().some(u => u.id === id)) {
            setMsg("signupId","이미 사용 중인 아이디입니다.","error");
            idChecked = false;
        } else {
            setMsg("signupId","사용 가능한 아이디입니다.","success");
            idChecked = true;
        }
    });

    // 주소 검색 (데모)
    addrSearchBtn.addEventListener("click", () => {
        addrMain.value = "서울특별시 강남구 테헤란로 (샘플)";
        toast("주소가 선택되었습니다.");
    });

    form.addEventListener("submit", (e) => {
        e.preventDefault();

        if (!idChecked) { toast("아이디 중복확인을 해주세요."); return; }
        const pwRule = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&]).{8,20}$/;
        if (!pwRule.test(pwInput.value)) { setMsg("signupPw","문자/숫자/특수문자 포함 8~20자","error"); return; }
        if (pwInput.value !== pw2Input.value) { setMsg("signupPw2","비밀번호가 일치하지 않습니다.","error"); return; }

        if (!idInput.value || !nameInput.value || !emailId.value || !emailDomain.value || !phone.value || !addrMain.value) {
            toast("필수 항목을 모두 입력하세요."); return;
        }

        const db = loadUsers();
        db.push({
            id: idInput.value.trim(),
            password: pwInput.value,
            name: nameInput.value.trim(),
            email: `${emailId.value.trim()}@${emailDomain.value.trim()}`,
            phone: phone.value.trim(),
            address: `${addrMain.value.trim()} ${addrDetail.value.trim()}`.trim()
        });
        saveUsers(db);

        toast("회원가입 완료! 로그인으로 이동합니다.", "success");
        setTimeout(() => location.href = "login.html", 900);
    });
})();

// ===== 로그인 =====
(() => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    const idInput = document.getElementById("loginId");
    const pwInput = document.getElementById("loginPw");

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const id = idInput.value.trim();
        const pw = pwInput.value;

        if (!id || !pw) { toast("아이디/비밀번호를 입력하세요."); return; }

        const user = loadUsers().find(u => u.id === id && u.password === pw);
        if (!user) { toast("아이디 또는 비밀번호가 올바르지 않습니다."); return; }

        localStorage.setItem("currentUser", JSON.stringify(user));
        toast(`${user.name}님 환영합니다!`, "success");
        // 필요 시 로그인 후 이동 경로 수정
        // location.href = "index.html";
    });
})();
