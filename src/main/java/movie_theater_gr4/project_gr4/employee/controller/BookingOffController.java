package movie_theater_gr4.project_gr4.employee.controller;

import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoicePromotionRepository;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoiceRepository;
import movie_theater_gr4.project_gr4.bookingMember.service.InvoiceService;
import movie_theater_gr4.project_gr4.bookingMember.service.SeatMemberService;
import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.employee.service.BookingServiceOfEmployee;
import movie_theater_gr4.project_gr4.mapper.AccountMapper;
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.repository.PromotionRepository;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.BookingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/employee")
public class BookingOffController {
    @Autowired
    private SeatMemberService seatMemberService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private BookingListService bookingListService;
    @Autowired
    private InvoicePromotionRepository invoicePromotionRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private BookingServiceOfEmployee bookingServiceOfEmployee;
    @GetMapping("/bookingOff")
    public String handleOfflineBooking(
            @RequestParam("amount") Long amount,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam("movieName") String movieName,
            @RequestParam(value = "promotionId", required = false) Integer promotionId,
            @RequestParam("finalTotal") Long finalTotal,
            @RequestParam(value = "usedScore", defaultValue = "0") int usedScore,
            @RequestParam("bookingDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
            @RequestParam("scheduleId") Long scheduleId,
            @RequestParam("scheduleSeatIds") List<Integer> scheduleSeatIds,
            @RequestParam(value = "memberId", required = false) String memberId,
            HttpSession session,
            Model model
    ) {
;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        int seatCount = scheduleSeatIds.size();
        int addScore = seatCount * 10;
        Integer memberID = null;
        LocalDateTime now = LocalDateTime.now();

        MemberDTO member = new MemberDTO();
        Date sqlDate = Date.valueOf(bookingDate);
        String seatNumbers = invoiceService.getSeatNamesByScheduleSeatIds(scheduleSeatIds.stream()
                .map(Integer::intValue)
                .collect(Collectors.toList()));
        Employee employee = invoiceService.findByAccountUsername(username);
        if (!Objects.equals(memberId, null)) {
            member = seatMemberService.getMemberByMemberId(memberId);
            memberID = member.getMemberId();
            invoiceService.createInvoice(memberID,finalTotal, "credit_card",
                    usedScore, addScore, 1, movieName,seatNumbers, now, employee.getEmployeeId());
            invoiceService.updateScore(member.getMemberId(), (addScore - usedScore));
        }else{
            invoiceService.createInvoice(memberID,finalTotal, "credit_card",
                    0, 0, 1, movieName,seatNumbers, now, employee.getEmployeeId() );
        }

        invoiceService.setStatusForListSeats(scheduleSeatIds);
        List<InvoiceDTO> bookingList = new ArrayList<>();
        Optional<InvoiceDTO> latestInvoice = Optional.empty();
        bookingList = bookingServiceOfEmployee.getLatestInvoiceByEmployeeId(employee.getEmployeeId());
        latestInvoice = bookingList.stream()
                .max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));

        Promotion promotion = null;
        if (promotionId != null && promotionId != 0) {
            Optional<Promotion> promotionOptional = promotionRepository.findById(promotionId);
            if (promotionOptional.isPresent()) {
                promotion = promotionOptional.get();
                Optional<Invoice> invoiceEntityOptional = invoiceRepository.findById(latestInvoice.get().getInvoiceId());
                if (invoiceEntityOptional.isPresent()) {
                    Invoice invoice = invoiceEntityOptional.get();
                    invoicePromotionRepository.save(new InvoicePromotion(invoice, promotion));
                    if (promotion.getMaxUsage() != null) {
                        promotion.setMaxUsage(promotion.getMaxUsage() - 1);
                        promotionRepository.save(promotion);
                    }
                } else {
                    System.out.println("Không tìm thấy Invoice trong DB");
                }
            } else {
                System.out.println("Promotion ID không hợp lệ");
            }
        }
        List<Long> longSeatIds = scheduleSeatIds.stream()
                .map(Integer::longValue)
                .toList();
        for (Long scheduleSeatId : longSeatIds) {
            ScheduleSeat scheduleSeat = seatMemberService.getScheduleSeatsByIds(scheduleSeatId);
            BigDecimal price = scheduleSeat.getSeatPrice();
            long seatPrice = (price != null) ? price.longValue() : 0L;
            long id = 1;
            invoiceService.insertTicket(latestInvoice.get().getInvoiceId(), scheduleSeatId, id, seatPrice);
        }
        model.addAttribute("message", "Đặt vé offline thành công!");

        Long movieId = (Long) session.getAttribute("movieId");
        Long showtimeId = (Long) session.getAttribute("showtimeId");
        LocalDate showDate = (LocalDate) session.getAttribute("showDate");

        session.removeAttribute("movieId");
        session.removeAttribute("showtimeId");
        session.removeAttribute("showDate");

        return "redirect:/employee/seatSelection?movieId=" + movieId +
                "&showtimeId=" + showtimeId +
                "&showDate=" + showDate;

    }

}
