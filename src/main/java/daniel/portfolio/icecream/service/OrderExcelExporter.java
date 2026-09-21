package daniel.portfolio.icecream.service;

import daniel.portfolio.icecream.repository.projection.OrderExportProjection;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import daniel.portfolio.icecream.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderExcelExporter {

    private static final String[] HEADERS = {
            "Order reference",
            "Placed at (UTC)",
            "Status",
            "Customer",
            "Product",
            "Quantity",
            "Unit price",
            "Line total"
    };
    // Owned here rather than by the service: the do/while below terminates on
    // a short page, so the size and the condition that reads it belong together.
    private static final int PAGE_SIZE = 500;

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private final AdminOrderService adminOrderService;

    public void writeTo(OutputStream outputStream, OrderStatus status, Instant from, Instant to)
            throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Orders");
            writeHeader(workbook, sheet);

            int rowIndex = 1;
            Instant lastCreatedAt = null;
            UUID lastItemId = null;
            List<OrderExportProjection> rows;

            do {
                rows = adminOrderService.findExportPage(status, from, to, lastCreatedAt, lastItemId, PAGE_SIZE);
                for (OrderExportProjection exportRow : rows) {
                    writeRow(sheet.createRow(rowIndex++), exportRow);
                }
                if (!rows.isEmpty()) {
                    OrderExportProjection last = rows.getLast();
                    lastCreatedAt = last.getPlacedAt();
                    lastItemId = last.getItemId();
                }
            } while (rows.size() == PAGE_SIZE);

            workbook.write(outputStream);
        }
    }

    private void writeHeader(SXSSFWorkbook workbook, Sheet sheet) {
        Font bold = workbook.createFont();
        bold.setBold(true);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(bold);

        Row header = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeRow(Row row, OrderExportProjection exportRow) {
        row.createCell(0).setCellValue(OrderReference.display(exportRow.getReference()));
        row.createCell(1).setCellValue(TIMESTAMP.format(exportRow.getPlacedAt()));
        row.createCell(2).setCellValue(exportRow.getStatus().name());
        row.createCell(3).setCellValue(exportRow.getCustomerEmail());
        row.createCell(4).setCellValue(exportRow.getProductName());
        row.createCell(5).setCellValue(exportRow.getQuantity());
        row.createCell(6).setCellValue(exportRow.getUnitPrice().doubleValue());
        row.createCell(7).setCellValue(exportRow
                .getUnitPrice()
                .multiply(BigDecimal.valueOf(exportRow.getQuantity()))
                .doubleValue()
        );
    }
}
