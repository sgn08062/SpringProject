// /js/address-list.js (address.js를 기준으로 동작시키는 얇은 오케스트레이터)

// 선택: 로딩 스켈레톤/에러/빈 상태 뷰 (유지하고 싶으면 남겨두세요)
function renderSkeleton(n=3){
    return Array.from({length:n}).map(()=>`
    <article class="card">
      <div class="dot" style="opacity:.35"></div>
      <div class="body">
        <div class="row">
          <div class="name skeleton" style="width:120px;height:16px"></div>
          <span class="badge skeleton" style="width:60px;height:20px"></span>
        </div>
        <div class="addr skeleton" style="height:16px;margin-top:6px"></div>
        <div class="addr skeleton" style="height:16px;width:70%;margin-top:6px"></div>
        <div class="meta skeleton" style="height:12px;width:40%;margin-top:8px"></div>
      </div>
    </article>
  `).join("");
}

document.addEventListener("DOMContentLoaded", async () => {
    // 스켈레톤 먼저 뿌리기 (선택)
    const container = document.querySelector("#list");
    if (container) container.innerHTML = renderSkeleton(4);

    // 핵심: address.js의 함수만 호출
    try {
        await fetchList(); // address.js
        render();          // address.js
    } catch (e) {
        console.error(e);
        // 필요 시 스켈레톤/에러 뷰 표시 로직 추가 가능
        // container.innerHTML = renderError();
    }
});
