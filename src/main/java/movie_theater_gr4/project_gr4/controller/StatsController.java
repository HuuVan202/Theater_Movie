package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.dto.*;
import movie_theater_gr4.project_gr4.service.StatsService;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("admin/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/all")
    public String showAllStats(
            // Revenue Filter Parameters
            @RequestParam(required = false) String revenueMode,
            @RequestParam(required = false) String revenueYear,
            @RequestParam(required = false) String selectedMonth,
            @RequestParam(required = false) String selectedQuarter,

            Model model) {

        if (revenueMode == null || revenueMode.isEmpty()) {
            revenueMode = "month";
        }
        if (revenueYear == null || revenueYear.isEmpty()) {
            revenueYear = String.valueOf(LocalDate.now().getYear());
        }

        handleRevenueFilter(revenueMode, revenueYear, selectedMonth, selectedQuarter, model);

        // Add static data (không phụ thuộc vào filter)
        model.addAttribute("preBookedStats", statsService.getPreBookedTicketStats());
        model.addAttribute("topCustomerStatsList", statsService.getTopCustomerStats());
        model.addAttribute("tierStats", statsService.getTierStats());
        model.addAttribute("totalAccounts",
                statsService.getTierStats().stream().mapToLong(TierStatsDTO::getTotal).sum());
        model.addAttribute("dailyRevenueStats", statsService.getDailyRevenueStats());
        model.addAttribute("genreRevenues", statsService.findGenreRevenueStats());
        model.addAttribute("peakHourRevenues", statsService.findPeakHourRevenueStats());

        return "stats/all-stats";
    }
    private void handleRevenueFilter(String revenueMode, String revenueYear, String month, String quarter, Model model) {
        model.addAttribute("revenueMode", revenueMode);

        switch (revenueMode) {
            case "day":
                if (revenueYear != null && month != null) {
                    model.addAttribute("revenueStats", statsService.getRevenueByMonthYear(revenueYear, month));
                } else {
                    LocalDate now = LocalDate.now();
                    String nowYear = String.valueOf(now.getYear());
                    String nowMonth = String.format("%02d", now.getMonthValue());
                    model.addAttribute("revenueStats", statsService.getRevenueByMonthYear(nowYear, nowMonth));
                }
                model.addAttribute("selectedRevenueYear", revenueYear);
                model.addAttribute("selectedMonth", month);
                model.addAttribute("selectedQuarter", null);
                break;
            case "month":
                model.addAttribute("revenueStats", statsService.getRevenueByMonth(revenueYear));
                model.addAttribute("selectedRevenueYear", revenueYear);
                model.addAttribute("selectedMonth", null);
                model.addAttribute("selectedQuarter", null);
                break;
            case "quarter":
                model.addAttribute("revenueStats", statsService.getRevenueByQuarter(revenueYear, quarter));
                model.addAttribute("selectedRevenueYear", revenueYear);
                model.addAttribute("selectedMonth", null);
                model.addAttribute("selectedQuarter", quarter);
                break;
            case "compareYear":
                model.addAttribute("revenueStats", statsService.getRevenueByYear());
                model.addAttribute("selectedRevenueYear", null);
                model.addAttribute("selectedMonth", null);
                model.addAttribute("selectedQuarter", null);
                break;
            default:
                model.addAttribute("revenueStats", statsService.getRevenueByMonth(revenueYear));
                model.addAttribute("selectedRevenueYear", revenueYear);
                model.addAttribute("selectedMonth", null);
                model.addAttribute("selectedQuarter", null);
                break;
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStatsToExcel(
            @RequestParam String filterType,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String monthYear,
            @RequestParam(required = false) String yearInput,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) throws IOException {

        LocalDate startDate, endDate;
        String filenameSuffix;

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        switch (filterType) {
            case "day" -> {
                if (day == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");
                startDate = endDate = LocalDate.parse(day);
                filenameSuffix = "_ngay_" + startDate.format(dateFmt);
            }
            case "week" -> {
                LocalDate now = LocalDate.now();
                startDate = now.with(DayOfWeek.MONDAY);
                endDate = now.with(DayOfWeek.SUNDAY);
                filenameSuffix = "_tuan_" + startDate.format(dateFmt) + "_den_" + endDate.format(dateFmt);
            }
            case "month" -> {
                YearMonth ym = (monthYear != null && !monthYear.isEmpty())
                        ? YearMonth.parse(monthYear)
                        : YearMonth.now();
                startDate = ym.atDay(1);
                endDate = ym.atEndOfMonth();
                filenameSuffix = "_thang_" + ym.getMonthValue() + "_nam_" + ym.getYear();
            }
            case "year" -> {
                int year = (yearInput != null) ? Integer.parseInt(yearInput) : LocalDate.now().getYear();
                startDate = LocalDate.of(year, 1, 1);
                endDate = LocalDate.of(year, 12, 31);
                filenameSuffix = "_nam_" + year;
            }
            case "range" -> {
                if (fromDate == null || toDate == null)
                    throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và kết thúc.");
                startDate = LocalDate.parse(fromDate);
                endDate = LocalDate.parse(toDate);
                filenameSuffix = "_tungay_" + startDate.format(dateFmt) + "_den_" + endDate.format(dateFmt);
            }
            default -> throw new IllegalArgumentException("Kiểu lọc không hợp lệ: " + filterType);
        }

        ByteArrayOutputStream out = statsService.exportMovieStatsExcel(startDate, endDate, filterType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("doanhthu" + filenameSuffix + ".xlsx")
                .build());

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }




}