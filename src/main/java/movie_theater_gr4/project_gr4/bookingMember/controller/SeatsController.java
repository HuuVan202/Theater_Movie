package movie_theater_gr4.project_gr4.bookingMember.controller;


import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;

import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.SeatSelectionInfoDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.ShowtimeDetailDTO;
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.dto.PromotionDTO;
import movie_theater_gr4.project_gr4.mapper.AccountMapper;

import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.repository.MovieOfPromotionRepository;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.bookingMember.service.SeatMemberService;
import movie_theater_gr4.project_gr4.service.BookingListService;
import movie_theater_gr4.project_gr4.bookingMember.service.InvoiceService;
import movie_theater_gr4.project_gr4.service.MovieService;
import movie_theater_gr4.project_gr4.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class SeatsController {
        @Autowired
        private SeatMemberService seatMemberService;
        @Autowired
        private AccountService accountService;
        @Autowired
        private AccountMapper accountMapper;
        @Autowired
        private SeatService seatService;
        @Autowired
        private MovieOfPromotionRepository movieOfPromotionRepository;
        @Autowired
        private InvoiceService invoiceService;
        @Autowired
        private BookingListService bookingListService;
        @Autowired
        private MovieService movieService;


        private final Map<Long, Instant> heldSeats = new ConcurrentHashMap<>();
        private final Duration HOLD_DURATION = Duration.ofMinutes(5);
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


        @PostMapping("/selectSeats")
        public String selectSeats(@RequestParam("versionId") Long versionId,
                                  @RequestParam("scheduleId") Long scheduleId,
                                  @RequestParam("movieId") Long movieId,
                                  Model model) {


            List<SeatSelectionInfoDTO> seats = seatMemberService.getSeatsByShowtime(scheduleId, movieId, versionId);

            List<SeatSelectionInfoDTO> coupleSeats = seats.stream()
                    .filter(seat -> seat.getSeatTypeId() == 3)
                    .sorted(Comparator.comparingLong(SeatSelectionInfoDTO::getSeatId)) // sắp xếp theo seatId tăng dần
                    .collect(Collectors.toList());

            Map<Integer, SeatSelectionInfoDTO> mapSeats = new LinkedHashMap<>();

            int index = 1;
            for (SeatSelectionInfoDTO seat : coupleSeats) {
                mapSeats.put(index++, seat);
            }
            long id = seats.get(1).getShowTimeId();
            ShowtimeDetailDTO showtimeDetailDTO = seatMemberService.getMovieInfoOfSelectSeatByShowtimeId(id);
            model.addAttribute("mapSeatsCouple", mapSeats);
            model.addAttribute("movieId", movieId);
            model.addAttribute("seats", seats);
            model.addAttribute("movie", showtimeDetailDTO);
            return "selectSeats";
        }

        @PostMapping("/confirm")
        public String confirmBooking(
                @RequestParam(required = false) String selectedSeats,
                @RequestParam(required = false) Integer quantity,
                @RequestParam(required = false) BigDecimal totalPrice,
                @RequestParam(required = false) String seatIds,
                @RequestParam(required = false) String scheduleSeatIds,
                @RequestParam(required = false) Long showtimeId,
                @RequestParam(required = false) String expireAt,
                @RequestParam(required = false) Long movieId,
                @RequestParam(required = false) Long invoiceId,
                @RequestParam(required = false) Integer status,
                HttpSession session,
                Model model){

            if (status != null && status == 0) {
                try {
                    selectedSeats = (String) session.getAttribute("selectedSeats");
                    quantity = (Integer) session.getAttribute("quantity");
                    totalPrice = (BigDecimal) session.getAttribute("totalPrice");
                    seatIds = (String) session.getAttribute("seatIds");
                    scheduleSeatIds = (String) session.getAttribute("scheduleSeatIds");
                    showtimeId = (Long) session.getAttribute("showtimeId");
                    expireAt = (String) session.getAttribute("expireAt");
                    movieId = (Long) session.getAttribute("movieId");
                    invoiceId = (Long) session.getAttribute("invoiceId");


                } catch (Exception e) {
                    System.out.println("Lỗi khi lấy từ session: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            final Integer finalQuantity = quantity;
//            final int todayDayOfWeekFinal = todayDayOfWeek;
//            final LocalDateTime nowFinal = now;
            List<String> seatList = new ArrayList<>();
            List<Long> seatIdList = new ArrayList<>();
            List<Long> scheduleSeatIdList = new ArrayList<>();

                 seatList = Arrays.asList(selectedSeats.split(","));
                 seatIdList = Arrays.stream(seatIds.split(",")).map(Long::parseLong).toList();
                 scheduleSeatIdList = Arrays.stream(scheduleSeatIds.split(",")).map(Long::parseLong).toList();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            AccountProflieDTO account = accountMapper.toAccountProflieDTO(accountService.findAccountByUsername(username));
            model.addAttribute("account", account);
            MemberDTO member = seatMemberService.getMemberByUsername(username);
            ShowtimeDetailDTO showtimeDetailDTO = seatMemberService.getMovieInfoOfSelectSeatByShowtimeId(showtimeId);

            List<Promotion> allPromotions = seatMemberService.getAllPromotions();
            int todayDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
            LocalDateTime now = LocalDateTime.now();

            List<MovieOfPromotion> movieOfPromoList = movieOfPromotionRepository.findByMovie_MovieId(movieId);
            Set<Integer> promotionIdsForMovie = movieOfPromoList.stream()
                    .map(mp -> mp.getPromotion().getPromotionId())
                    .collect(Collectors.toSet());

            List<MovieOfPromotion> allMovieOfPromoList = movieOfPromotionRepository.findAll();
            Set<Integer> allMovieSpecificPromoIds = allMovieOfPromoList.stream()
                    .map(mp -> mp.getPromotion().getPromotionId())
                    .collect(Collectors.toSet());

            Predicate<Promotion> isValidPromotion = promo -> {
                boolean minTickets = promo.getMinTickets() == null || promo.getMinTickets() <= finalQuantity;
                if (!minTickets) return false;
                boolean dayOfWeek = promo.getDayOfWeek() == null || promo.getDayOfWeek() == todayDayOfWeek;
                if (!dayOfWeek) return false;
                boolean isActive = promo.isActive();
                if (!isActive) return false;
                boolean startTime = promo.getStartTime() == null || !promo.getStartTime().isAfter(now);
                if (!startTime) return false;
                boolean endTime = promo.getEndTime() == null || !promo.getEndTime().isBefore(now);
                return endTime;
            };
            List<InvoicePromotion> promoOfInvoiceList = seatMemberService.findInvoicePromotionAllByUsername(username);

            List<Promotion> movieSpecificPromotions = movieOfPromoList.stream()
                    .map(MovieOfPromotion::getPromotion)
                    .filter(isValidPromotion)
                    .filter(promo -> promoOfInvoiceList.stream()
                            .noneMatch(invPromo -> invPromo.getPromotion().getPromotionId().equals(promo.getPromotionId())))
                    .collect(Collectors.toList());

            List<Promotion> nonMoviePromotions = allPromotions.stream()
                    .filter(promo -> !allMovieSpecificPromoIds.contains(promo.getPromotionId()))
                    .filter(isValidPromotion)
                    .filter(promo -> promoOfInvoiceList.stream()
                            .noneMatch(invPromo -> invPromo.getPromotion().getPromotionId().equals(promo.getPromotionId())))
                    .collect(Collectors.toList());

            List<Promotion> matchedPromotions = new ArrayList<>();
            matchedPromotions.addAll(movieSpecificPromotions);
            matchedPromotions.addAll(nonMoviePromotions);

            model.addAttribute("promotionList", matchedPromotions.stream()
                    .map(promo -> {
                        PromotionDTO dto = new PromotionDTO();
                        dto.setPromotionId(promo.getPromotionId());
                        dto.setDetail(promo.getDetail());
                        dto.setDiscountLevel(promo.getDiscountLevel());
                        dto.setDiscountAmount(promo.getDiscountAmount());
                        return dto;
                    })
                    .collect(Collectors.toList()));

            session.setAttribute("selectedSeats", selectedSeats);
            session.setAttribute("quantity", quantity);
            session.setAttribute("totalPrice", totalPrice);
            session.setAttribute("seatIds", seatIds);
            session.setAttribute("scheduleSeatIds", scheduleSeatIds);
            session.setAttribute("showtimeId", showtimeId);
            session.setAttribute("expireAt", expireAt);
            session.setAttribute("movieId", movieId);
            session.setAttribute("invoiceId", invoiceId);


            model.addAttribute("promotionList", matchedPromotions);
            model.addAttribute("seatList", seatList);
            model.addAttribute("seatIdList", seatIdList);
            model.addAttribute("scheduleSeatIdList", scheduleSeatIdList);
            model.addAttribute("quantity", quantity);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("movie", showtimeDetailDTO);
            model.addAttribute("member", member);
            model.addAttribute("expireAt", expireAt);
            model.addAttribute("invoiceId", invoiceId);
//            session.removeAttribute("selectedSeats");
//            session.removeAttribute("quantity");
//            session.removeAttribute("totalPrice");
//            session.removeAttribute("seatIds");
//            session.removeAttribute("scheduleSeatIds");
//            session.removeAttribute("showtimeId");
//            session.removeAttribute("expireAt");
//            session.removeAttribute("movieId");
//            session.removeAttribute("invoiceId");
            return "confirm";
        }


        @ResponseBody
        @PostMapping("/seats/hold")
        public ResponseEntity<?> holdSeats(@RequestBody Map<String, Object> requestBody,    HttpSession session) {

            List<Integer> seatIdsInt = (List<Integer>) requestBody.get("scheduleSeatIds");
            List<Long> scheduleSeatIds = seatIdsInt.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());
            Integer movieIdInt = (Integer) requestBody.get("movieId");
            Long movieId = movieIdInt != null ? movieIdInt.longValue() : null;


            // Create response map
            Map<String, Object> response = new HashMap<>();
            // Lấy thông tin người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            MemberDTO member = seatMemberService.getMemberByUsername(username);
            Integer accountId = member != null ? member.getAccountId() : null;
            MovieDTO movie = movieService.getMovieDetail(movieId);
            // Tạo Invoice với status = 0
            long totalPrice = calculateTotalPrice(scheduleSeatIds); // Hàm tính tổng giá, bạn cần triển khai
            String seatNumbers = invoiceService.getSeatNamesByScheduleSeatIds(scheduleSeatIds.stream()
                    .map(Long::intValue)
                    .collect(Collectors.toList()));
            LocalDateTime nows = LocalDateTime.now();


            invoiceService.createInvoice(
                    accountId,
                    totalPrice,
                    "online",
                    0,
                    0,
                    0,
                    movie.getMovieName(),
                    seatNumbers,
                    nows,
                    null
            );


            AccountProflieDTO accountProflieDTO = accountMapper.toAccountProflieDTO(accountService.findAccountByUsername(username));

            String keyword = accountProflieDTO.getIsGoogle() == null
                    ? accountProflieDTO.getUsername()
                    : accountProflieDTO.getEmail();

            List<InvoiceDTO> bookingList = bookingListService.getAllInvoiceByKeyword(keyword);

            Optional<InvoiceDTO> latestInvoice = bookingList.stream()
                    .max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));

            Instant expireAt = Instant.now().plus(HOLD_DURATION);
            for (Long id : scheduleSeatIds) {
                heldSeats.put(id, expireAt);
//                System.out.println("Giữ ghế ID " + id + " đến " + expireAt);
            }

            seatService.updateScheduleSeatStatus(scheduleSeatIds, 1); // Set status = 1 (HELD)

            // Lên lịch giải phóng ghế
            scheduler.schedule(() -> {
                Instant now = Instant.now();
                List<Long> expiredIds = new ArrayList<>();
                for (Long id : scheduleSeatIds) {
                    if (heldSeats.containsKey(id) && heldSeats.get(id).isBefore(now)) {
                        expiredIds.add(id);
                        heldSeats.remove(id);
                    }

                }
//                System.out.println("đã hết time");
                if (!expiredIds.isEmpty() && bookingListService.getInvoiceById(latestInvoice.get().getInvoiceId()).getStatus()!=1) {
//                    System.out.println("fail");
                    seatService.updateScheduleSeatStatus(expiredIds, 0); // Trả ghế về AVAILABLE
                    invoiceService.updateInvoiceStatus(latestInvoice.get().getInvoiceId(), -1);
//                    System.out.println("Ghế hết hạn giữ: " + expiredIds + ", Invoice ID: " + latestInvoice.get().getInvoiceId() + " đã hủy");
                    session.removeAttribute("selectedSeats");
                    session.removeAttribute("quantity");
                    session.removeAttribute("totalPrice");
                    session.removeAttribute("seatIds");
                    session.removeAttribute("scheduleSeatIds");
                    session.removeAttribute("showtimeId");
                    session.removeAttribute("expireAt");
                    session.removeAttribute("movieId");
                    session.removeAttribute("invoiceId");
                }
            }, HOLD_DURATION.toSeconds(), TimeUnit.SECONDS);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "expireAt", expireAt.toString(),
                    "invoiceId", latestInvoice.get().getInvoiceId()
            ));
        }

        // Hàm tính tổng giá (cần triển khai)
        private long calculateTotalPrice(List<Long> scheduleSeatIds) {
            return scheduleSeatIds.stream()
                    .map(id -> seatMemberService.getScheduleSeatsByIds(id))
                    .filter(Objects::nonNull)
                    .map(ScheduleSeat::getSeatPrice)
                    .filter(Objects::nonNull)
                    .mapToLong(BigDecimal::longValue)
                    .sum();
        }


}
