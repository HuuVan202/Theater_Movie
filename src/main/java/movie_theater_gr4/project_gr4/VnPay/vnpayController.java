package movie_theater_gr4.project_gr4.VnPay;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoicePromotionRepository;
import movie_theater_gr4.project_gr4.bookingMember.repository.InvoiceRepository;
import movie_theater_gr4.project_gr4.bookingMember.service.InvoiceService;
import movie_theater_gr4.project_gr4.bookingMember.service.SeatMemberService;
import movie_theater_gr4.project_gr4.dto.*;
import movie_theater_gr4.project_gr4.employee.service.BookingServiceOfEmployee;
import movie_theater_gr4.project_gr4.employee.service.SelectMovieService;
import movie_theater_gr4.project_gr4.employee.service.SelectSeatService;
import movie_theater_gr4.project_gr4.mapper.AccountMapper;
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.repository.PromotionRepository;
import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import movie_theater_gr4.project_gr4.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/payment")
public class vnpayController {
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
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SelectMovieService selectMovieService;


    @GetMapping("/create_payment")
    public void createPayment(
            @RequestParam("finalTotal") long amount,
            @RequestParam("orderInfo") String orderInfo,
            @RequestParam("movieName") String movieName,
            @RequestParam(value = "promotionId", required = false) Integer selectedPromotionId,
            @RequestParam("bookingDate") String date,
            @RequestParam(value = "usedScore", required = false) Integer usedScore,
            @RequestParam("scheduleSeatIds") List<Integer> seatIds,
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            @RequestParam(required = false) String memberId,

            HttpServletResponse response
    ) throws IOException {
        String orderType = "other";
        amount = amount * 100;
        String vnp_TxnRef = "ORDER" + System.currentTimeMillis();
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        String rawOrderInfo = movieName;
        String encodedOrderInfo = URLEncoder.encode(rawOrderInfo, StandardCharsets.UTF_8.toString());
        vnp_Params.put("vnp_OrderInfo", encodedOrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");

        String seatParams = seatIds.stream()
                .map(id -> "scheduleSeatIds=" + id)
                .collect(Collectors.joining("&"));
        String returnUrlWithSeats = Config.vnp_ReturnUrl + "?" + seatParams;
        if (usedScore != null) returnUrlWithSeats += "&usedScore=" + usedScore;
        if (date != null) returnUrlWithSeats += "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8);
        if (memberId != null) returnUrlWithSeats += "&memberId=" + memberId;
        if (selectedPromotionId != null) returnUrlWithSeats += "&selectedPromotionId=" + selectedPromotionId;
        if (invoiceId != null) returnUrlWithSeats += "&invoiceId=" + invoiceId; // Thêm invoiceId

        vnp_Params.put("vnp_ReturnUrl", returnUrlWithSeats);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        String paymentUrl = Config.vnp_PayUrl + "?" + query;

        response.sendRedirect(paymentUrl);
    }

    @GetMapping("/payment_infor")
    public String transaction(
            @RequestParam("vnp_Amount") String amount,
            @RequestParam("vnp_BankCode") String bankCode,
            @RequestParam("vnp_OrderInfo") String orderInfo,
            @RequestParam("vnp_ResponseCode") String responseCode,
            @RequestParam("vnp_TxnRef") String txnRef,
            @RequestParam(value = "vnp_TransactionNo", required = false) String transactionNo,
            @RequestParam(value = "vnp_PayDate", required = false) String payDate,
            @RequestParam("scheduleSeatIds") List<Integer> seatIds,
            @RequestParam(value = "usedScore", defaultValue = "0") int usedScore,
            @RequestParam("date") String dateString,
            @RequestParam(required = false) String memberId,
            @RequestParam(value = "selectedPromotionId", required = false) Integer selectedPromotionId,
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,

            HttpSession session,
            Model model) throws ParseException {

        String decodedOrderInfo = java.net.URLDecoder.decode(orderInfo, StandardCharsets.UTF_8);
        // tên phim
        model.addAttribute("vnp_OrderInfo", decodedOrderInfo);
        model.addAttribute("vnp_TxnRef", txnRef);
        model.addAttribute("vnp_Amount", Long.parseLong(amount) / 100);
        model.addAttribute("vnp_ResponseCode", responseCode);
        model.addAttribute("vnp_TransactionNo", transactionNo);
        model.addAttribute("vnp_BankCode", bankCode);

        String formattedDate = "";
        if (payDate != null && !payDate.isEmpty()) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                Date dateObj = (Date) inputFormat.parse(payDate);
                formattedDate = outputFormat.format(dateObj);
            } catch (Exception e) {
                formattedDate = payDate;
            }
        }
        // Ngày
        model.addAttribute("vnp_PayDate", formattedDate);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int accountId = ((CustomUserDetails) authentication.getPrincipal()).getAccountId();
        String username = authentication.getName();
        String role = authentication.getAuthorities().toString();
        System.out.println(role);
        if (responseCode.equals("00")) {
            // Thanh toán thành công
            int seatCount = seatIds.size();
            int addScore = seatCount * 10;
            long parsedAmount = Long.parseLong(amount) / 100;

            //Lấy ghế
            List<Long> longSeatIds = seatIds.stream()
                    .map(Integer::longValue)
                    .toList();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Ngày booking
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            LocalDateTime bookingDate = localDate.atStartOfDay(); // 00:00:00

            Integer memberID = null;

            // Tên Phim
            String decoded = URLDecoder.decode(orderInfo, StandardCharsets.UTF_8);
            // Danh sách ghế
            String seatNumbers = invoiceService.getSeatNamesByScheduleSeatIds(seatIds.stream()
                    .map(Integer::intValue)
                    .collect(Collectors.toList()));
            Optional<InvoiceDTO> latestInvoice = Optional.empty();
            // Ngày booking
            Date sqlDate = Date.valueOf(bookingDate.toLocalDate());
            LocalDateTime now = LocalDateTime.now();

            MemberDTO member = new MemberDTO();
            AccountProflieDTO accountProflieDTO = accountMapper.toAccountProflieDTO(accountService.findAccountByUsername(username));
            List<InvoiceDTO> bookingList = new ArrayList<>();
            if (role.equals("[ROLE_MEMBER]")) {
                member = seatMemberService.getMemberByUsername(username);
                memberID = member.getAccountId();
                invoiceService.updateScore(member.getMemberId(), (addScore - usedScore));
                invoiceService.updateInvoiceDetails(invoiceId, parsedAmount,usedScore, addScore, decodedOrderInfo, now, 1);

                String titleNotification = "\uD83C\uDF89 ĐẶT VÉ THÀNH CÔNG!";
                String contentNotification = String.format(
                        "Xin chúc mừng! Bạn đã đặt vé thành công cho bộ phim %s.\n\n" +
                                "\uD83D\uDCCD Thông tin chi tiết:\n\n" +
                                "\uD83C\uDFAC Phim: %s\n\n" +
                                "\uD83D\uDDD3 Ngày chiếu: %s\n\n" +
//                                "⏰ Giờ chiếu: %s\n\n" +
                                "\uD83E\uDE91 Ghế: %s\n\n" +
                                "\uD83C\uDFE2 Rạp: %s\n\n" +
                                "\uD83D\uDCA1 Lưu ý:\n" +
                                "Vui lòng có mặt tại rạp trước giờ chiếu ít nhất 15 phút để làm thủ tục và nhận vé.\n\n" +
                                "Cảm ơn bạn đã đồng hành cùng MoonCinema! Chúc bạn có trải nghiệm điện ảnh tuyệt vời! \uD83C\uDF7F\uD83C\uDFA5",
                        decoded, decoded, sqlDate, seatNumbers, "MoonCinema"
                );

//                notificationService.sendAndSaveNotification(accountId,titleNotification,contentNotification);

                String keyword = accountProflieDTO.getIsGoogle() == null
                        ? accountProflieDTO.getUsername()
                        : accountProflieDTO.getEmail();
                bookingList = bookingListService.getAllInvoiceByKeyword(keyword);

                List<Map<String, Object>> combinedBookings = bookingList.stream().map(booking -> {
                    Map<String, Object> combined = new HashMap<>();
                    combined.put("booking", booking);
                    combined.put("bookingInfo", selectMovieService.findBookingInfoByInvoiceId(booking.getInvoiceId()));
                    return combined;
                }).collect(Collectors.toList());

                latestInvoice = bookingList.stream()
                        .max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));


                model.addAttribute("account", accountProflieDTO);
            } else if (role.equals("[ROLE_EMPLOYEE]")) {
                Employee employee = invoiceService.findByAccountUsername(username);
                if (!Objects.equals(memberId, null)) {
                    member = seatMemberService.getMemberByMemberId(memberId);
                    memberID = member.getMemberId();
                    invoiceService.createInvoice(memberID,parsedAmount, "online",
                            usedScore, addScore, 1, decoded,seatNumbers, now, employee.getEmployeeId());
                    invoiceService.updateScore(member.getMemberId(), (addScore - usedScore));
                }else{
                    invoiceService.createInvoice(memberID,parsedAmount, "online",
                            0, 0, 1, decoded,seatNumbers, now, employee.getEmployeeId() );
                }
                bookingList = bookingServiceOfEmployee.getLatestInvoiceByEmployeeId(employee.getEmployeeId());
                latestInvoice = bookingList.stream()
                        .max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));
            }

            invoiceService.setStatusForListSeats(seatIds);

            // Lưu khuyến mãi (nếu có)
            Promotion promotion = null;
            if (selectedPromotionId != null && selectedPromotionId != 0) {
                Optional<Promotion> promotionOptional = promotionRepository.findById(selectedPromotionId);
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

            for (Long scheduleSeatId : longSeatIds) {
                ScheduleSeat scheduleSeat = seatMemberService.getScheduleSeatsByIds(scheduleSeatId);
                BigDecimal price = scheduleSeat.getSeatPrice();
                long seatPrice = (price != null) ? price.longValue() : 0L;
                long id = 1;
                invoiceService.insertTicket(latestInvoice.get().getInvoiceId(), scheduleSeatId, id, seatPrice);
            }


            session.removeAttribute("selectedSeats");
            session.removeAttribute("quantity");
            session.removeAttribute("totalPrice");
            session.removeAttribute("seatIds");
            session.removeAttribute("scheduleSeatIds");
            session.removeAttribute("expireAt");
            session.removeAttribute("invoiceId");
            model.addAttribute("bookingList", bookingList);
            model.addAttribute("member", member);
        } else {
            // Thanh toán thất bại, cập nhật trạng thái Invoice thành -1 (hủy)
            invoiceService.updateInvoiceStatus(invoiceId, -1);
//            seatService.updateScheduleSeatStatus(longSeatIds, 0);
            model.addAttribute("transactionStatus", "Thất bại");
        }

        if (role.equals("[ROLE_EMPLOYEE]")) {
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

        return "redirect:/profile";

    }
}
