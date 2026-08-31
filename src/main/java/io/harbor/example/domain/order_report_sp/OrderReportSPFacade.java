package io.harbor.example.domain.order_report_sp;

import io.harbor.api.HarborSession;
import io.harbor.example.domain.order_report.dto.OrderReport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderReportSPFacade {
    private final HarborSession harborSession;

    public List<OrderReport> findOrderReports(@NonNull String series, @NonNull String code) {
        OrderReportSPFunction fn = new OrderReportSPFunction(null);
        return harborSession.select(fn.call(series, code))
                .fetchAll()
                .stream()
                .map(OrderReportSP::toDto)
                .toList();
    }
}
