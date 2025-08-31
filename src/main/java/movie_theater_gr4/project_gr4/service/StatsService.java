package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.dto.*;
import movie_theater_gr4.project_gr4.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private AccountRepository accountRepository;

    public List<PreBookedTicketStatsDTO> getPreBookedTicketStats() {
        return statisticRepository.findPreBookedTicketStats();
    }

    public List<TopCustomerStatsDTO> getTopCustomerStats() {
        return statisticRepository.findTopCustomerStats();
    }

    public List<TierStatsDTO> getTierStats() {
        List<Object[]> rawStats = accountRepository.countAccountByRole();
        List<TierStatsDTO> result = new ArrayList<>();

        for (Object[] row : rawStats) {
            String tier = row[0] != null ? row[0].toString() : "Không xác định";
            Long total = ((Number) row[1]).longValue();
            result.add(new TierStatsDTO(tier, total));
        }

        return result;
    }

    public List<RevenueStatsDTO> getRevenueByMonth(String year) {
        List<RevenueStatsDTO> rawData = statisticRepository.getRevenueByMonth(year);

        // Dùng Map để ánh xạ dữ liệu doanh thu theo tháng (key = "01" đến "12")
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (RevenueStatsDTO dto : rawData) {
            // Đảm bảo định dạng 2 chữ số: "01", "02", ..., "12"
            String monthKey = String.format("%02d", Integer.parseInt(dto.getMonth()));
            revenueMap.put(monthKey, dto.getTotalRevenue());
        }

        // Tạo danh sách 12 tháng đầy đủ
        List<RevenueStatsDTO> fullList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            BigDecimal revenue = revenueMap.getOrDefault(month, BigDecimal.ZERO);
            fullList.add(new RevenueStatsDTO(month, revenue));
        }

        return fullList;
    }

    public List<RevenueStatsDTO> getRevenueByYear() {
        return statisticRepository.getRevenueByYearFrom2023();
    }

    public List<DailyRevenueStatsDTO> getDailyRevenueStats() {
        return statisticRepository.getDailyRevenueStatsRaw().stream()
                .map(row -> new DailyRevenueStatsDTO(
                        (String) row[0],
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<RevenueStatsDTO> getRevenueByQuarter(String year, String quarter) {
        return statisticRepository.getRevenueByQuarter(year, quarter);
    }

    public List<RevenueStatsDTO> getRevenueByMonthYear(String year, String month) {
        return statisticRepository.getRevenueByMonthYear(year, month);
    }

    public List<GenreRevenueDTO> findGenreRevenueStats(){
        return statisticRepository.findGenreRevenueStats();
    }

    public List<PeakHourRevenueDTO> findPeakHourRevenueStats(){
        return statisticRepository.findPeakHourRevenueStats();
    }

    public ByteArrayOutputStream exportMovieStatsExcel(LocalDate startDate, LocalDate endDate, String filterType) throws IOException {
        // 1. Lấy dữ liệu gốc và map sang DTO
        List<Object[]> rawData = statisticRepository.getMovieExcelStats(startDate.toString(), endDate.toString());
        List<MovieExcelStatsDTO> data = rawData.stream()
                .map(r -> new MovieExcelStatsDTO(
                        (String) r[0],                     // Tên phim
                        (String) r[1],                     // Thể loại
                        ((Number) r[2]).longValue(),       // Vé bán
                        ((Number) r[3]).longValue(),       // Số suất
                        ((Number) r[4]).longValue(),       // Vé đặt trước
                        (BigDecimal) r[5]                  // Doanh thu
                ))
                .toList();

        // 2. Tạo Workbook & Sheet
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Thống kê phim");

        // 3. Tạo font Unicode dùng chung
        Font unicodeFont = workbook.createFont();
        unicodeFont.setFontName("Times New Roman");
        unicodeFont.setFontHeightInPoints((short) 11);

        // 4. Tạo các style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle textStyle = createTextStyle(workbook, unicodeFont);
        CellStyle numberStyle = createNumberStyle(workbook, unicodeFont);
        CellStyle moneyStyle = createMoneyStyle(workbook, unicodeFont);

        // 5. Tạo dòng tiêu đề
        String[] headers = { "Tên phim", "Thể loại", "Số vé bán", "Số suất chiếu", "Số vé đặt trước", "Doanh thu (VNĐ)" };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));

        List<RevenueExcelDTO> revenueStats = getRevenueStats(startDate, endDate, filterType);
        XSSFSheet sheet2 = workbook.createSheet("Thống kê doanh thu");
        String[] revHeaders = {"Thời gian", "Doanh thu (VNĐ)"};
        Row headerRow2 = sheet2.createRow(0);
        for (int i = 0; i < revHeaders.length; i++) {
            Cell cell = headerRow2.createCell(i);
            cell.setCellValue(revHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        int row2 = 1;
        for (RevenueExcelDTO rev : revenueStats) {
            Row row = sheet2.createRow(row2++);
            writeCell(row, 0, rev.getLabel(), textStyle);
            writeCell(row, 1, rev.getTotalRevenue(), moneyStyle);
        }
        sheet2.setAutoFilter(new CellRangeAddress(0, 0, 0, revHeaders.length - 1));
        for (int i = 0; i < revHeaders.length; i++) {
            sheet2.autoSizeColumn(i);
            sheet2.setColumnWidth(i, sheet2.getColumnWidth(i) + 1024);
        }

        // 6. Ghi dữ liệu
        int rowNum = 1;
        for (MovieExcelStatsDTO dto : data) {
            Row row = sheet.createRow(rowNum++);
            writeCell(row, 0, dto.getMovieName(), textStyle);
            writeCell(row, 1, dto.getGenreNames(), textStyle);
            writeCell(row, 2, dto.getTicketsSold(), numberStyle);
            writeCell(row, 3, dto.getShowCount(), numberStyle);
            writeCell(row, 4, dto.getPreBookedTickets(), numberStyle);
            writeCell(row, 5, dto.getTotalRevenue(), moneyStyle);
        }

        // 7. Resize cột và tránh che icon filter
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
        }

        // 8. Ghi ra file
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTextStyle(XSSFWorkbook workbook, Font font) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(XSSFWorkbook workbook, Font font) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createMoneyStyle(XSSFWorkbook workbook, Font font) {
        CellStyle style = createNumberStyle(workbook, font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private void writeCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeCell(Row row, int colIndex, long value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void writeCell(Row row, int colIndex, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value.doubleValue()); // Excel không hỗ trợ BigDecimal trực tiếp
        cell.setCellStyle(style);
    }

    public List<RevenueExcelDTO> getRevenueStats(LocalDate start, LocalDate end, String filterType) {
        List<Object[]> raw;

        long days = ChronoUnit.DAYS.between(start, end) + 1;
        switch (filterType) {
            case "day" -> raw = statisticRepository.getRevenueGroupedByDay(
                    start.withDayOfMonth(1), start.withDayOfMonth(start.lengthOfMonth()));
            case "week" -> raw = statisticRepository.getRevenueGroupedByWeek(
                    LocalDate.now().withDayOfMonth(1), LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
            case "month" -> raw = statisticRepository.getRevenueGroupedByMonth(
                    start.withMonth(1).withDayOfMonth(1), start.withMonth(12).withDayOfMonth(31));
            case "year" -> raw = statisticRepository.getRevenueGroupedByYear(LocalDate.of(2020,1,1), LocalDate.now());
            case "range" -> {
                if (days >= 365) raw = statisticRepository.getRevenueGroupedByYear(start, end);
                else if (days >= 30) raw = statisticRepository.getRevenueGroupedByMonth(start, end);
                else if (days >= 7) raw = statisticRepository.getRevenueGroupedByWeek(start, end);
                else raw = statisticRepository.getRevenueGroupedByDay(start, end);
            }
            default -> throw new IllegalArgumentException("Bộ lọc không hợp lệ: " + filterType);
        }

        return raw.stream()
                .map(r -> new RevenueExcelDTO(
                        (String) r[0],
                        r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO
                ))
                .toList();
    }


}