function resetSearch() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';

    $('.date-tab').removeClass('active');
    $('.date-tab[data-period="365"]').addClass('active');

    const $form = $('#searchForm');
    $form.find('input[name="page"]').val(1);
    $form.find('input[name="size"]').val(10);
    $form.submit();
}

function formatDate(date) {
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('-');
}

function getDaysDifference(startDateStr, endDateStr) {
    const date1 = new Date(startDateStr);
    const date2 = new Date(endDateStr);
    const oneDay = 1000 * 60 * 60 * 24;

    const diffTime = date2.getTime() - date1.getTime();
    return Math.round(diffTime / oneDay);
}

function setDateRange(periodDays) {
    const today = new Date();
    const endDate = formatDate(today);

    const startDateObj = new Date(today);
    startDateObj.setDate(today.getDate() - periodDays);
    const startDate = formatDate(startDateObj);

    $('#startDate').val(startDate);
    $('#endDate').val(endDate);
}

function openModal(contentHtml) {
    $('#modalBodyContent').html(contentHtml);
    $('#orderDetailModal').css('display', 'block');
}

function closeModal() {
    $('#orderDetailModal').css('display', 'none');
    $('#modalBodyContent').html('<p>상세 내용을 불러오는 중입니다...</p>');
}

$(document).ready(function() {
    const $searchForm = $('#searchForm');

    // 탭 상태 유지 로직 (변경 없음)
    const startDate = $('#startDate').val();
    const endDate = $('#endDate').val();
    let isTabActivated = false;

    if (startDate && endDate) {
        const daysDifference = getDaysDifference(startDate, endDate);

        $('.date-tab').each(function() {
            const period = parseInt($(this).data('period'), 10);

            if (period === daysDifference) {
                $('.date-tab').removeClass('active');
                $(this).addClass('active');
                isTabActivated = true;
                return false;
            }
        });

        if (!isTabActivated) {
            $('.date-tab').removeClass('active');
        }

    } else {
        setDateRange(365);
        $('.date-tab[data-period="365"]').addClass('active');
    }

    // ⭐ [수정] 날짜 탭 클릭 이벤트: 날짜만 설정하고 검색은 실행하지 않음
    $('.date-tab').on('click', function() {
        const period = $(this).data('period');

        $('.date-tab').removeClass('active');
        $(this).addClass('active');

        // 날짜 범위만 설정
        setDateRange(period);

        // 이전 코드: $searchForm.find('input[name="page"]').val(1); $searchForm.submit(); (삭제됨)
    });

    // ⭐ [수정] 검색 폼 제출 이벤트: '조회' 버튼이 눌렸을 때만 페이지를 1로 초기화하고 검색 실행
    $searchForm.on('submit', function(e) {
        // 검색 실행 전에 페이지를 1로 초기화
        $searchForm.find('input[name="page"]').val(1);
    });

    $('#startDate, #endDate').on('change', function() {
        $('.date-tab').removeClass('active');
    });

    $('.detail-button').on('click', function(e) {
        e.preventDefault();
        const orderId = $(this).data('order-id');

        $('#modalBodyContent').html('<p>주문 ID ' + orderId + '의 상세 내용을 불러오는 중입니다...</p>');
        openModal($('#modalBodyContent').html());

        $.ajax({
            url: '/uorder/detail/' + orderId,
            type: 'GET',
            success: function(data) {
                openModal(data);
            },
            error: function(xhr, status, error) {
                let errorMessage = "주문 상세 정보를 불러오는 데 실패했습니다.";
                if (xhr.status === 404) {
                    errorMessage = "주문 정보를 찾을 수 없습니다. (ID: " + orderId + ")";
                }
                openModal('<p style="color: red;">' + errorMessage + '</p>');
            }
        });
    });

    $('.close-button').on('click', closeModal);
    $(window).on('click', function(event) {
        if (event.target === document.getElementById('orderDetailModal')) {
            closeModal();
        }
    });

    $('.cancel-button').on('click', function() {
        const orderId = $(this).data('order-id');
        const statusText = $(this).text().trim();

        if (statusText === '취소 완료') {
            alert("이미 취소된 주문입니다.");
            return;
        }
        if ($(this).prop('disabled')) {
            alert("현재 주문 상태(" + statusText + ")에서는 취소가 불가능합니다.");
            return;
        }

        if (confirm(orderId + "번 주문을 정말 취소하시겠습니까? 주문 취소 시 재고가 복구됩니다.")) {

            const $form = $('#cancelForm');
            $form.attr('action', '/uorder/cancel/' + orderId);

            $form.submit();
        }
    });
});