package com.example.myFarm.order;

import com.example.myFarm.command.OrderAmountDTO;
import com.example.myFarm.command.OrderAmountVO;
import com.example.myFarm.command.OrderVO;
import com.example.myFarm.inventory.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {
    @Autowired
    AdminOrderMapper adminOrderMapper;
    @Autowired
    OrderAmountMapper orderAmountMapper;
    @Autowired
    InventoryService inventoryService;

    @Override
    public List<OrderVO> list() {
        return adminOrderMapper.list();
    }

    @Override
    public OrderAmountDTO findById(long orderId) {
        return orderAmountMapper.findById(orderId);
    }

    // ✅ 상태 변경 + "주문 취소"로 바뀔 때 재고 복원
    @Override
    @Transactional
    public int updateStatus(long orderId, String status) {

        // 1. 기존 상태 조회
        String prevStatus = adminOrderMapper.getStatus(orderId);
        if (prevStatus == null) {
            return 0; // 존재하지 않는 주문
        }

        // 주문 취소 상태일 경우 더이상 상태 변경 불가
        if ("주문 취소".equals(prevStatus)) {
            return -1; // 특별 코드
        }

        // 2. 상태 업데이트
        int updated = adminOrderMapper.updateStatus(orderId, status);
        if (updated != 1) {
            return updated;
        }

        // 3. 이전 상태 ≠ "주문 취소" 이고, 새 상태 = "주문 취소" 일 때만 재고 복원
        if (!"주문 취소".equals(prevStatus) && "주문 취소".equals(status)) {

            // 주문 상세 가져오기 (주문 아이템 + CROP_ID + 수량)
            OrderAmountDTO dto = orderAmountMapper.findById(orderId);
            if (dto != null && dto.getOrderAmountList() != null) {
                for (OrderAmountVO item : dto.getOrderAmountList()) {

                    Long cropId = item.getCropId();
                    long qty = item.getQuantity();

                    if (cropId != null && qty > 0) {
                        // ✅ INVENTORY.AMOUNT += 주문 수량
                        inventoryService.addAmount(cropId, qty);
                    }
                }
            }
        }

        return updated;
    }

    @Override
    public List<OrderVO> getFilteredOrderList(Map<String, Object> conditions) {
        return adminOrderMapper.getFilteredOrderList(conditions);
    }
}
