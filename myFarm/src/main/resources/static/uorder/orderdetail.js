const cancelUrl = window.orderCancelUrl || '/uorder/cancel';

/**
 * 주문 취소 처리 함수
 * @param {number} orderId
 */
function cancelOrder(orderId) {
    if (!confirm(`${orderId}번 주문을 정말로 취소하시겠습니까?`)) {
        return;
    }

    $.ajax({
        // Controller의 POST /uorder/cancel/{orderId} 경로 사용
        url: `${cancelUrl}/${orderId}`,
        type: 'POST',
        success: function(response) {
            // 서버 응답 메시지를 표시하거나 기본 성공 메시지를 사용
            alert(response.message || "주문이 성공적으로 취소되었습니다.");
            // 취소 후 페이지 새로고침하여 상태 업데이트
            location.reload();
        },
        error: function(xhr) {
            let errorMessage = "주문 취소 중 오류가 발생했습니다.";
            try {
                // 서버에서 JSON 응답으로 에러 메시지를 보낼 경우
                const responseJson = JSON.parse(xhr.responseText);
                errorMessage = responseJson.message || responseJson.message || JSON.stringify(responseJson);
            } catch (e) {
                // JSON 형식이 아니거나 파싱 실패 시
                if (xhr.responseText) {
                    errorMessage = xhr.responseText;
                }
            }
            alert(errorMessage);
        }
    });
}