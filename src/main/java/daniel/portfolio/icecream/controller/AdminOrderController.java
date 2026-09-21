package daniel.portfolio.icecream.controller;

import daniel.portfolio.icecream.controller.response.AdminOrderResponse;
import daniel.portfolio.icecream.controller.response.OrderAnalyticsResponse;
import daniel.portfolio.icecream.model.OrderStatus;
import daniel.portfolio.icecream.service.AdminOrderService;
import daniel.portfolio.icecream.service.OrderExcelExporter;
import daniel.portfolio.icecream.swagger.AdminOrderApiDocs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderExcelExporter orderExcelExporter;

    @GetMapping
    @AdminOrderApiDocs
    public ResponseEntity<@NonNull PagedModel<@NonNull AdminOrderResponse>> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminOrderResponse> orders = adminOrderService.findOrders(status, from, to, pageable);
        return ResponseEntity.ok(new PagedModel<>(orders));
    }

    @GetMapping("/summary")
    @AdminOrderApiDocs
    public ResponseEntity<@NonNull OrderAnalyticsResponse> summary(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        return ResponseEntity.ok(adminOrderService.analytics(from, to));
    }

    @GetMapping("/export")
    @AdminOrderApiDocs
    public ResponseEntity<@NonNull StreamingResponseBody> export(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        String filename = "orders-" + LocalDate.now(ZoneOffset.UTC) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(outputStream -> orderExcelExporter.writeTo(outputStream, status, from, to));
    }
}
