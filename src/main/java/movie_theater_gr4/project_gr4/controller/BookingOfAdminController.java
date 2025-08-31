package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.employee.dto.BookingDTO;
import movie_theater_gr4.project_gr4.employee.service.SelectSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookingList")
public class BookingOfAdminController {
    @Autowired
    private SelectSeatService selectSeatService;

    @GetMapping
    public String getBookings(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "startId", defaultValue = "0") long startId,
            @RequestParam(name = "date", required = false) String date,
            @RequestParam(name = "movieName", required = false) String movieName,
            @RequestParam(name = "status", required = false) String status,
            Model model) {
        int pageSize = 15;

        // Lấy tất cả booking và sắp xếp theo invoiceId
        List<BookingDTO> allBookings = selectSeatService.getAllBooking()
                .stream()
                .sorted(Comparator.comparingLong(BookingDTO::getInvoiceId))
                .collect(Collectors.toList());

        // Lọc danh sách booking dựa trên các tham số
        List<BookingDTO> filteredBookings = allBookings;

        if (date != null && !date.isEmpty()) {
            try {
                LocalDate filterDate = LocalDate.parse(date);
                filteredBookings = filteredBookings.stream()
                        .filter(booking -> booking.getBookingDate() != null &&
                                booking.getBookingDate().toLocalDateTime().toLocalDate().equals(filterDate))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Invalid date format: " + date);
            }
        }

        if (movieName != null && !movieName.isEmpty()) {
            filteredBookings = filteredBookings.stream()
                    .filter(booking -> booking.getMovieName() != null &&
                            booking.getMovieName().toLowerCase().contains(movieName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            try {
                int statusValue = Integer.parseInt(status); // Chuyển status từ chuỗi sang số nguyên
                filteredBookings = filteredBookings.stream()
                        .filter(booking -> booking.getStatus() == statusValue)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                System.err.println("Invalid status value: " + status);
            }
        }

        // Tính tổng số trang
        int totalBookings = filteredBookings.size();
        int totalPages = (int) Math.ceil((double) totalBookings / pageSize);

        // Đảm bảo trang hiện tại hợp lệ
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        // Lấy danh sách booking cho trang hiện tại từ filteredBookings
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredBookings.size());
        List<BookingDTO> bookingListPage = filteredBookings.isEmpty() ?
                Collections.emptyList() :
                filteredBookings.subList(startIndex, endIndex);

        // Tạo danh sách startIds từ filteredBookings
        List<Long> startIds = new ArrayList<>();
        for (int i = 0; i < filteredBookings.size(); i += pageSize) {
            if (i < filteredBookings.size()) {
                startIds.add(filteredBookings.get(i).getInvoiceId());
            }
        }

        // Tính startId cho trang trước và trang sau
        long prevStartId = (page > 1 && !startIds.isEmpty()) ? startIds.get(page - 2) : 0;
        long nextStartId = (page < totalPages && !startIds.isEmpty()) ? startIds.get(page) : 0;

        // Thêm các thuộc tính vào model
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("bookings", bookingListPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIds", startIds);
        model.addAttribute("prevStartId", prevStartId);
        model.addAttribute("nextStartId", nextStartId);
        model.addAttribute("totalBookings", totalBookings);

        // Chỉ thêm các tham số lọc vào model nếu chúng không rỗng
        if (date != null && !date.isEmpty()) {
            model.addAttribute("date", date);
        }
        if (movieName != null && !movieName.isEmpty()) {
            model.addAttribute("movieName", movieName);
        }
        if (status != null && !status.isEmpty()) {
            model.addAttribute("status", status);
        }

        return "admin/bookingList";
    }
}