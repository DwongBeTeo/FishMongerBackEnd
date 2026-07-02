package datn.duong.FishSeller.service;

import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.dto.dashboard.TopProductDTO;
import datn.duong.FishSeller.dto.dashboard.DailyRevenueDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelService {

    public ByteArrayInputStream exportDashboardToExcel(DashboardStatisticsDTO stats) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- SHEET 1: TỔNG QUAN ---
            Sheet sheetOverview = workbook.createSheet("Tổng Quan");
            
            // Style cho Header (In đậm, nền xanh nhạt)
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Tạo Header
            Row headerRow = sheetOverview.createRow(0);
            String[] columns = {"Chỉ số", "Giá trị", "So sánh tháng trước"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dữ liệu Tổng quan
            int rowIdx = 1;
            
            // Doanh thu
            Row revRow = sheetOverview.createRow(rowIdx++);
            revRow.createCell(0).setCellValue("Doanh thu tháng này");
            Cell cellRev = revRow.createCell(1);
            cellRev.setCellValue(stats.getTotalRevenueThisMonth());
            cellRev.setCellStyle(currencyStyle);
            
            String growthText = (stats.getGrowthRate() > 0 ? "Tăng " : "Giảm ") + stats.getGrowthRate() + "%";
            revRow.createCell(2).setCellValue(growthText + " (Tháng trước: " + formatCurrency(stats.getTotalRevenueLastMonth()) + ")");

            // Đơn hàng
            Row orderRow = sheetOverview.createRow(rowIdx++);
            orderRow.createCell(0).setCellValue("Tổng đơn hàng");
            orderRow.createCell(1).setCellValue(stats.getTotalOrdersThisMonth());

            // Khách hàng
            Row custRow = sheetOverview.createRow(rowIdx++);
            custRow.createCell(0).setCellValue("Tổng khách hàng");
            custRow.createCell(1).setCellValue(stats.getTotalCustomers());

            // Auto size cột
            for(int i = 0; i < 3; i++) sheetOverview.autoSizeColumn(i);

            // --- SHEET 2: TOP SẢN PHẨM ---
            Sheet sheetProduct = workbook.createSheet("Top Sản Phẩm");
            Row headerProd = sheetProduct.createRow(0);
            String[] colProd = {"ID", "Tên Sản Phẩm", "Số lượng bán", "Doanh thu"};
            for (int i = 0; i < colProd.length; i++) {
                Cell cell = headerProd.createCell(i);
                cell.setCellValue(colProd[i]);
                cell.setCellStyle(headerStyle);
            }

            int prodRowIdx = 1;
            if (stats.getTopProducts() != null) {
                for (TopProductDTO prod : stats.getTopProducts()) {
                    Row row = sheetProduct.createRow(prodRowIdx++);
                    row.createCell(0).setCellValue(prod.getId());
                    row.createCell(1).setCellValue(prod.getName());
                    row.createCell(2).setCellValue(prod.getTotalSold());
                    
                    Cell revCell = row.createCell(3);
                    revCell.setCellValue(prod.getTotalRevenue() != null ? prod.getTotalRevenue() : 0);
                    revCell.setCellStyle(currencyStyle);
                }
            }
            for(int i = 0; i < 4; i++) sheetProduct.autoSizeColumn(i);
            
            // --- SHEET 3: CHI TIẾT NGÀY ---
             Sheet sheetDaily = workbook.createSheet("Chi tiết ngày");
             Row headerDaily = sheetDaily.createRow(0);
             headerDaily.createCell(0).setCellValue("Ngày");
             headerDaily.createCell(1).setCellValue("Doanh thu");
             headerDaily.getCell(0).setCellStyle(headerStyle);
             headerDaily.getCell(1).setCellStyle(headerStyle);
             
             int dailyRowIdx = 1;
             if(stats.getDailyRevenues() != null) {
                 for(DailyRevenueDTO daily : stats.getDailyRevenues()) {
                     Row row = sheetDaily.createRow(dailyRowIdx++);
                     row.createCell(0).setCellValue(daily.getDate().toString());
                     Cell c = row.createCell(1);
                     c.setCellValue(daily.getRevenue());
                     c.setCellStyle(currencyStyle);
                 }
             }
             sheetDaily.autoSizeColumn(0);
             sheetDaily.autoSizeColumn(1);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi xuất file Excel: " + e.getMessage());
        }
    }

    // --- Helper Methods ---
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 ₫"));
        return style;
    }
    
    private String formatCurrency(Double amount) {
        return String.format("%,.0f ₫", amount);
    }
}