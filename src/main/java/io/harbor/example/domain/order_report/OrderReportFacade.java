package io.harbor.example.domain.order_report;

import io.harbor.example.domain.order_report.dto.CustomerSpending;
import io.harbor.example.domain.order_report.dto.OrderReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderReportFacade {
    private final OrderReportDAO orderReportDAO;

    public List<OrderReport> findAll() {
        return orderReportDAO.findAll().stream()
                .map(OrderReportView::toDto)
                .toList();
    }

    public List<CustomerSpending> findSpendingsAbove(BigDecimal minTotalGross) {
        return orderReportDAO.findSpendingsAbove(minTotalGross);
    }
}
