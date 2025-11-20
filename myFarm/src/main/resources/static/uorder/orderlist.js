function resetSearch() {
document.getElementById('startDate').value = '';
document.getElementById('endDate').value = '';

// 탭 초기화
$('.date-tab').removeClass('active');
$('.date-tab[data-period="365"]').addClass('active');

// 페이징 파라미터를 초기화하고 검색 폼을 제출 (첫 페이지로 돌아감)
const $form = $('#searchForm');
// page와 size를 초기화 (Controller의 @RequestParam defaultValue에 맞춤)
$form.find('input[name="page"]').val(1);
$form.find('input[name="size"]').val(10);
$form.submit();
}

// 날짜를 YYYY-MM-DD 형식으로 포맷
function formatDate(date) {
const d = new Date(date);
let month = '' + (d.getMonth() + 1);
let day = '' + d.getDate();
const year = d.getFullYear();

if (month.length < 2) month = '0' + month;
if (day.length < 2) day = '0' + day;

return [year, month, day].join('-');
}

// ⭐ [추가] 날짜 탭 클릭 이벤트 처리
function setDateRange(periodDays) {
const today = new Date();
const endDate = formatDate(today); // 오늘 날짜

// 시작일 계산: 오늘 날짜 - periodDays
const startDateObj = new Date(today);
startDateObj.setDate(today.getDate() - periodDays);
const startDate = formatDate(startDateObj);

$('#startDate').val(startDate);
$('#endDate').val(endDate);
}

$(document).ready(function() {
// 초기 로드 시 '최대(1년)' 탭 활성화 (Controller에서 날짜 파라미터가 없으면 1년 조회되도록 가정)
if (!$('#startDate').val() && !$('#endDate').val()) {
setDateRange(365); // 초기 로딩 시 1년으로 설정
}

$('.date-tab').on('click', function() {
const period = $(this).data('period');

// 탭 활성화/비활성화
$('.date-tab').removeClass('active');
$(this).addClass('active');

// 날짜 범위 설정
setDateRange(period);
});

// 날짜 필드가 변경되면 탭 비활성화
$('#startDate, #endDate').on('change', function() {
$('.date-tab').removeClass('active');
});

// 주문 취소 로직 (기존 코드 유지)
$('.cancel-button').on('click', function() {
const orderId = $(this).data('order-id');
const statusText = $(this).text().trim();

if (statusText === '취소 완료') {
alert("이미 취소된 주문입니다.");
return;
}
if ($(this).prop('disabled')) {
// '배송 중' 또는 '배송완료' 상태일 때 disabled 속성이 적용됨
alert("현재 주문 상태(" + statusText + ")에서는 취소가 불가능합니다.");
return;
}

if (confirm(orderId + "번 주문을 정말 취소하시겠습니까? 주문 취소 시 재고가 복구됩니다.")) {

const $form = $('#cancelForm');
// OrderController의 POST 경로에 맞게 action을 설정
$form.attr('action', '/uorder/cancel/' + orderId);

$form.submit();
}
});
});