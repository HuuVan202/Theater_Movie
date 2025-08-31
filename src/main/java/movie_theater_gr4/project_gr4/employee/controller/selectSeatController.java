package movie_theater_gr4.project_gr4.employee.controller;

import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.SeatSelectionInfoDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingDTO;
import movie_theater_gr4.project_gr4.employee.dto.MemberDTO;
import movie_theater_gr4.project_gr4.employee.dto.SelectMovieDTO;
import movie_theater_gr4.project_gr4.employee.service.SelectMovieService;
import movie_theater_gr4.project_gr4.employee.service.SelectSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller

@RequestMapping("/employee")
public class selectSeatController {
    @Autowired
    private SelectMovieService selectMovieService;
    @Autowired
    private SelectSeatService selectSeatService;


    @GetMapping("/seatSelection")
    public String handleSeatSelection(@RequestParam Long movieId,
                                      @RequestParam Long showtimeId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
                                      HttpSession session,
                                      Model model) {
        List<SelectMovieDTO> allMovies = selectMovieService.getInfoShowtimeByMovieIdAndShowtimeId(movieId,showtimeId);
        List<SeatSelectionInfoDTO> seats = selectMovieService.getSeatsByShowtime(showtimeId, movieId);
        List<Long> listScheduleSeatId = new ArrayList<>();
        for (SeatSelectionInfoDTO seat : seats) {
            listScheduleSeatId.add(seat.getScheduleSeatId());
        }
        List<SeatSelectionInfoDTO> coupleSeats = seats.stream()
                .filter(seat -> seat.getSeatTypeId() == 3)
                .sorted(Comparator.comparingLong(SeatSelectionInfoDTO::getSeatId)) // sắp xếp theo seatId tăng dần
                .collect(Collectors.toList());

        Map<Integer, SeatSelectionInfoDTO> mapSeats = new LinkedHashMap<>();

        int index = 1;
        for (SeatSelectionInfoDTO seat : coupleSeats) {
            mapSeats.put(index++, seat);
        }
        List<BookingDTO> listInvoice = selectSeatService.getListInvoice(listScheduleSeatId);
        session.setAttribute("movieId", movieId);
        session.setAttribute("showtimeId", showtimeId);
        session.setAttribute("showDate", showDate);

        model.addAttribute("mapSeatsCouple", mapSeats);
        model.addAttribute("seats", seats);
        model.addAttribute("listShowtimeMovie", allMovies);
        model.addAttribute("showDate", showDate);
        model.addAttribute("listInvoice", listInvoice);
        return "employee/seat_selection_UseFrag";
    }

    @GetMapping("/check")
    public ResponseEntity<MemberDTO> checkMember(@RequestParam String keyword) {
        Optional<MemberDTO> optionalMember = selectSeatService.findByKeyword(keyword);

//        optionalMember.ifPresent(member -> {
//            System.out.println("=== MemberDTO Details ===");
//            System.out.println("Member ID: " + member.getMemberId());
//            System.out.println("Account ID: " + member.getAccountId());
//            System.out.println("Username: " + member.getUsername());
//            System.out.println("Full Name: " + member.getScore());
//        });

        return optionalMember
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
