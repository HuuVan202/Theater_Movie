package movie_theater_gr4.project_gr4.employee.controller;

import movie_theater_gr4.project_gr4.employee.dto.BookingDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingInfoDTO;
import movie_theater_gr4.project_gr4.employee.dto.SelectMovieDTO;
import movie_theater_gr4.project_gr4.employee.dto.ShowtimeDTO;
import movie_theater_gr4.project_gr4.employee.service.SelectMovieService;
import movie_theater_gr4.project_gr4.employee.service.SelectSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employee")
public class SelectMovieController {

    @Autowired
    private SelectMovieService selectMovieService;

    @Autowired
    private SelectSeatService selectSeatService;

    @GetMapping("/showtime")
    public String showMovieSelectionPage(
            @RequestParam(name = "showDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate showDate,
            Model model) {

        if (showDate == null) {
            showDate = LocalDate.now();
        }

        LocalDate minDate = LocalDate.now();
        LocalDate maxDate = minDate.plusDays(14);

        List<SelectMovieDTO> allMovies = selectMovieService.getAllMoviesWithShowtimes();

        LocalDate finalShowDate = showDate;
        List<SelectMovieDTO> filteredMovies = allMovies.stream()
                .map(movie -> {
                    List<ShowtimeDTO> filteredShowtimes = movie.getShowtimes().stream()
                            .filter(showtime -> finalShowDate.equals(showtime.getShowDate()))
                            .collect(Collectors.toList());
                    if (!filteredShowtimes.isEmpty()) {
                        movie.setShowtimes(filteredShowtimes);
                        return movie;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<BookingDTO> allBookings = selectSeatService.getAllBooking().stream()
                .filter(booking -> booking.getBookingDate() != null &&
                        booking.getBookingDate().toLocalDateTime().toLocalDate().equals(finalShowDate))
                .sorted(Comparator.comparingLong(BookingDTO::getInvoiceId))
                .collect(Collectors.toList());

        List<Map<String, Object>> combinedBookings = allBookings.stream().map(booking -> {
            Map<String, Object> combined = new HashMap<>();
            combined.put("booking", booking);
            combined.put("bookingInfo", selectMovieService.findBookingInfoByInvoiceId(booking.getInvoiceId()));
            return combined;
        }).collect(Collectors.toList());

        model.addAttribute("allBookings", combinedBookings);
        model.addAttribute("listShowtimeMovie", filteredMovies);
        model.addAttribute("selectedDate", showDate);
        model.addAttribute("minDate", minDate);
        model.addAttribute("maxDate", maxDate);
        return "employee/movie_showtimes_UseFrag";
    }

    @GetMapping("/orderSearch")
    @ResponseBody
    public List<Map<String, Object>> searchBookings(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "showDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate showDate) {
        List<BookingDTO> bookings = selectSeatService.getAllBooking().stream()
                .filter(booking -> booking.getBookingDate() != null &&
                        booking.getBookingDate().toLocalDateTime().toLocalDate().equals(showDate))
                .filter(booking ->
                        (booking.getFullName() != null && booking.getFullName().toLowerCase().contains(query.toLowerCase())) ||
                                (booking.getPhoneNumber() != null && booking.getPhoneNumber().equals(query)) ||
                                (booking.getIdentityCard() != null && booking.getIdentityCard().equals(query)))
                .sorted(Comparator.comparingLong(BookingDTO::getInvoiceId))
                .collect(Collectors.toList());

        return bookings.stream().map(booking -> {
            Map<String, Object> combined = new HashMap<>();
            combined.put("booking", booking);
            combined.put("bookingInfo", selectMovieService.findBookingInfoByInvoiceId(booking.getInvoiceId()));
            return combined;
        }).collect(Collectors.toList());
    }

}