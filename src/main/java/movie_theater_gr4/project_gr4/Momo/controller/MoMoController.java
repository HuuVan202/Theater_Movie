//package movie_theater_gr4.project_gr4.Momo.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import movie_theater_gr4.project_gr4.Momo.util.MoMoUtils;
//import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
//import movie_theater_gr4.project_gr4.bookingMember.repository.InvoicePromotionRepository;
//import movie_theater_gr4.project_gr4.bookingMember.repository.InvoiceRepository;
//import movie_theater_gr4.project_gr4.bookingMember.service.InvoiceService;
//import movie_theater_gr4.project_gr4.bookingMember.service.SeatMemberService;
//import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
//import movie_theater_gr4.project_gr4.dto.MemberDTO;
//import movie_theater_gr4.project_gr4.employee.service.BookingServiceOfEmployee;
//import movie_theater_gr4.project_gr4.mapper.AccountMapper;
//import movie_theater_gr4.project_gr4.model.*;
//import movie_theater_gr4.project_gr4.repository.PromotionRepository;
//import movie_theater_gr4.project_gr4.service.AccountService;
//import movie_theater_gr4.project_gr4.service.BookingListService;
//import org.apache.hc.client5.http.classic.methods.HttpPost;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.io.entity.StringEntity;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//
//import java.io.IOException;
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.sql.Date;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/momo")
//public class MoMoController {
//
//    @Value("${momo.partner-code}")
//    private String partnerCode;
//
//    @Value("${momo.access-key}")
//    private String accessKey;
//
//    @Value("${momo.secret-key}")
//    private String secretKey;
//
//    @Value("${momo.request-type}")
//    private String requestType;
//
//    @Value("${momo.ipn-url}")
//    private String ipnUrl;
//
//    @Value("${momo.redirect-url}")
//    private String redirectUrl;
//
//    private final String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
//
//    @Autowired
//    private SeatMemberService seatMemberService;
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    private AccountMapper accountMapper;
//
//    @Autowired
//    private InvoiceService invoiceService;
//
//    @Autowired
//    private BookingListService bookingListService;
//
//    @Autowired
//    private InvoicePromotionRepository invoicePromotionRepository;
//
//    @Autowired
//    private PromotionRepository promotionRepository;
//
//    @Autowired
//    private InvoiceRepository invoiceRepository;
//
//    @Autowired
//    private BookingServiceOfEmployee bookingServiceOfEmployee;
//
//
//
//    /**
//     * Tạo thanh toán MoMo từ Booking đã có sẵn
//     */
//    @GetMapping("/pay")
//    public void payWithMomo(
//            @RequestParam int bookingId,
//            @RequestParam("movieName") String movieName,
//            @RequestParam("bookingDate") String date,
//            @RequestParam(value = "promotionId", required = false) Integer selectedPromotionId,
//            @RequestParam(value = "usedScore", required = false, defaultValue = "0") Integer usedScore,
//            @RequestParam("scheduleSeatIds") List<Integer> seatIds,
//            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
//            @RequestParam(required = false) String memberId,
//            HttpServletResponse response
//    ) throws IOException {
//
//
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
//
//        long amount = booking.getTotalPrice().longValue();
//        String orderInfo = "Thanh toán vé xem phim " + movieName;
//        String requestId = "ORDER" + System.currentTimeMillis();
//        String orderId = "ORDER" + System.currentTimeMillis();
//        String lang = "vi";
//        String extraData = String.valueOf(bookingId);
//
//        // Tạo chuỗi dữ liệu để ký
//        String rawData = String.format("accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
//                accessKey, amount, extraData, ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, requestType);
//        String signature;
//        try {
//            signature = MoMoUtils.generateSignature(rawData, secretKey);
//        } catch (Exception e) {
//            throw new IOException("Lỗi khi tạo chữ ký MoMo: " + e.getMessage());
//        }
//
//        // Tạo body yêu cầu
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("partnerCode", partnerCode);
//        requestBody.put("accessKey", accessKey);
//        requestBody.put("requestId", requestId);
//        requestBody.put("amount", String.valueOf(amount));
//        requestBody.put("orderId", orderId);
//        requestBody.put("orderInfo", orderInfo);
//        requestBody.put("redirectUrl", buildReturnUrl(seatIds, usedScore, date, memberId, selectedPromotionId, invoiceId));
//        requestBody.put("ipnUrl", ipnUrl);
//        requestBody.put("extraData", extraData);
//        requestBody.put("requestType", requestType);
//        requestBody.put("signature", signature);
//        requestBody.put("lang", lang);
//
//        // Gửi yêu cầu đến MoMo
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost httpPost = new HttpPost(endpoint);
//        httpPost.setHeader("Content-Type", "application/json");
//        try {
//            httpPost.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(requestBody), StandardCharsets.UTF_8));
//            CloseableHttpResponse momoResponse = client.execute(httpPost);
//            String responseBody = new String(momoResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
//            Map<String, Object> responseMap = new ObjectMapper().readValue(responseBody, Map.class);
//
//            if ((int) responseMap.get("resultCode") == 0) {
//                String payUrl = (String) responseMap.get("payUrl");
//                response.sendRedirect(payUrl);
//            } else {
//                throw new IOException("Lỗi từ MoMo: " + responseMap.get("message"));
//            }
//        } finally {
//            client.close();
//        }
//    }
//
//    // Phương thức tái sử dụng từ vnpayController
//    private String buildReturnUrl(List<Integer> seatIds, Integer usedScore, String date, String memberId, Integer selectedPromotionId, Long invoiceId) throws IOException {
//        String seatParams = seatIds.stream()
//                .map(id -> "scheduleSeatIds=" + id)
//                .collect(Collectors.joining("&"));
//        String returnUrlWithParams = redirectUrl + "?" + seatParams;
//        if (usedScore != null) returnUrlWithParams += "&usedScore=" + usedScore;
//        if (date != null) returnUrlWithParams += "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8);
//        if (memberId != null) returnUrlWithParams += "&memberId=" + memberId;
//        if (selectedPromotionId != null) returnUrlWithParams += "&selectedPromotionId=" + selectedPromotionId;
//        if (invoiceId != null) returnUrlWithParams += "&invoiceId=" + invoiceId;
//        return returnUrlWithParams;
//    }
//
//    /**
//     * IPN (notify từ MoMo)
//     */
//    @PostMapping("/ipn")
//    public ResponseEntity<String> handleMomoIPN(@RequestBody Map<String, Object> payload) {
//        try {
//            String signature = (String) payload.get("signature");
//            String extraData = (String) payload.get("extraData");
//            String rawData = String.format("accessKey=%s&amount=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
//                    accessKey, payload.get("amount"), payload.get("message"), payload.get("orderId"),
//                    payload.get("orderInfo"), payload.get("orderType"), partnerCode, payload.get("payType"),
//                    payload.get("requestId"), payload.get("responseTime"), payload.get("resultCode"), payload.get("transId"));
//
//            String computedSignature = MoMoUtils.generateSignature(rawData, secretKey);
//
//            if (computedSignature.equals(signature)) {
//                int resultCode = (int) payload.get("resultCode");
//                if (resultCode == 0) {
//                    int bookingId = Integer.parseInt(extraData);
//                    bookingService.confirmPayment(bookingId);
//                    return ResponseEntity.ok("ipn received");
//                }
//            }
//            return ResponseEntity.badRequest().body("invalid ipn: invalid signature or resultCode");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("invalid ipn: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Người dùng thanh toán xong sẽ được redirect về
//     */
//    @GetMapping("/return")
//    public String transaction(
//            @RequestParam("amount") String amount,
//            @RequestParam("orderInfo") String orderInfo,
//            @RequestParam("resultCode") String resultCode,
//            @RequestParam("orderId") String orderId,
//            @RequestParam("requestId") String requestId,
//            @RequestParam(value = "transId", required = false) String transId,
//            @RequestParam(value = "responseTime", required = false) String responseTime,
//            @RequestParam("extraData") String extraData,
//            @RequestParam("scheduleSeatIds") List<Integer> seatIds,
//            @RequestParam(value = "usedScore", defaultValue = "0") int usedScore,
//            @RequestParam("date") String dateString,
//            @RequestParam(required = false) String memberId,
//            @RequestParam(value = "selectedPromotionId", required = false) Integer selectedPromotionId,
//            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
//            HttpSession session,
//            Model model
//    ) throws Exception {
//        String decodedOrderInfo = URLDecoder.decode(orderInfo, StandardCharsets.UTF_8);
//        model.addAttribute("orderInfo", decodedOrderInfo);
//        model.addAttribute("orderId", orderId);
//        model.addAttribute("amount", Long.parseLong(amount));
//        model.addAttribute("resultCode", resultCode);
//        model.addAttribute("transId", transId);
//        model.addAttribute("requestId", requestId);
//
//        String formattedDate = "";
//        if (responseTime != null && !responseTime.isEmpty()) {
//            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//            try {
//                Date dateObj = inputFormat.parse(responseTime);
//                formattedDate = outputFormat.format(dateObj);
//            } catch (Exception e) {
//                formattedDate = responseTime;
//            }
//        }
//        model.addAttribute("responseTime", formattedDate);
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        String role = authentication.getAuthorities().toString();
//
//        int bookingId = Integer.parseInt(extraData);
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
//
//        if (resultCode.equals("0")) {
//            // Thanh toán thành công
//            bookingService.confirmPayment(bookingId);
//
//            int seatCount = seatIds.size();
//            int addScore = seatCount * 10;
//            long parsedAmount = Long.parseLong(amount);
//
//            List<Long> longSeatIds = seatIds.stream().map(Integer::longValue).toList();
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            LocalDate localDate = LocalDate.parse(dateString, formatter);
//            LocalDateTime bookingDate = localDate.atStartOfDay();
//
//            Integer memberID = null;
//            String seatNumbers = invoiceService.getSeatNamesByScheduleSeatIds(seatIds);
//            Optional<InvoiceDTO> latestInvoice = Optional.empty();
//            Date sqlDate = Date.valueOf(bookingDate.toLocalDate());
//            MemberDTO member = new MemberDTO();
//            AccountProflieDTO accountProflieDTO = accountMapper.toAccountProflieDTO(accountService.findAccountByUsername(username));
//            List<InvoiceDTO> bookingList = new ArrayList<>();
//
//            if (role.equals("[ROLE_MEMBER]")) {
//                member = seatMemberService.getMemberByUsername(username);
//                memberID = member.getAccountId();
//                invoiceService.updateScore(member.getMemberId(), (addScore - usedScore));
//                invoiceService.updateInvoiceDetails(invoiceId, parsedAmount, addScore, decodedOrderInfo, bookingDate, 1);
//
//                String keyword = accountProflieDTO.getIsGoogle() == null ? accountProflieDTO.getUsername() : accountProflieDTO.getEmail();
//                bookingList = bookingListService.getAllInvoiceByKeyword(keyword);
//                latestInvoice = bookingList.stream().max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));
//                model.addAttribute("account", accountProflieDTO);
//            } else if (role.equals("[ROLE_EMPLOYEE]")) {
//                Employee employee = invoiceService.findByAccountUsername(username);
//                if (memberId != null) {
//                    member = seatMemberService.getMemberByMemberId(memberId);
//                    memberID = member.getMemberId();
//                    invoiceService.createInvoice(memberID, parsedAmount, "online", usedScore, addScore, 1, decodedOrderInfo, seatNumbers, sqlDate, employee.getEmployeeId());
//                    invoiceService.updateScore(member.getMemberId(), (addScore - usedScore));
//                } else {
//                    invoiceService.createInvoice(memberID, parsedAmount, "online", 0, 0, 1, decodedOrderInfo, seatNumbers, sqlDate, employee.getEmployeeId());
//                }
//                bookingList = bookingServiceOfEmployee.getLatestInvoiceByEmployeeId(employee.getEmployeeId());
//                latestInvoice = bookingList.stream().max(Comparator.comparingLong(InvoiceDTO::getInvoiceId));
//            }
//
//            invoiceService.setStatusForListSeats(seatIds);
//
//            // Lưu khuyến mãi
//            Promotion promotion = null;
//            if (selectedPromotionId != null && selectedPromotionId != 0) {
//                Optional<Promotion> promotionOptional = promotionRepository.findById(selectedPromotionId);
//                if (promotionOptional.isPresent()) {
//                    promotion = promotionOptional.get();
//                    Optional<Invoice> invoiceEntityOptional = invoiceRepository.findById(latestInvoice.get().getInvoiceId());
//                    if (invoiceEntityOptional.isPresent()) {
//                        Invoice invoice = invoiceEntityOptional.get();
//                        invoicePromotionRepository.save(new InvoicePromotion(invoice, promotion));
//                        if (promotion.getMaxUsage() != null) {
//                            promotion.setMaxUsage(promotion.getMaxUsage() - 1);
//                            promotionRepository.save(promotion);
//                        }
//                    } else {
//                        System.out.println("Không tìm thấy Invoice trong DB");
//                    }
//                } else {
//                    System.out.println("Promotion ID không hợp lệ");
//                }
//            }
//
//            for (Long scheduleSeatId : longSeatIds) {
//                ScheduleSeat scheduleSeat = seatMemberService.getScheduleSeatsByIds(scheduleSeatId);
//                long seatPrice = scheduleSeat.getSeatPrice() != null ? scheduleSeat.getSeatPrice().longValue() : 0L;
//                long id = 1;
//                invoiceService.insertTicket(latestInvoice.get().getInvoiceId(), scheduleSeatId, id, seatPrice);
//            }
//
//            session.removeAttribute("selectedSeats");
//            session.removeAttribute("quantity");
//            session.removeAttribute("totalPrice");
//            session.removeAttribute("seatIds");
//            session.removeAttribute("scheduleSeatIds");
//            session.removeAttribute("showtimeId");
//            session.removeAttribute("expireAt");
//            session.removeAttribute("movieId");
//            session.removeAttribute("invoiceId");
//
//            model.addAttribute("bookingList", bookingList);
//            model.addAttribute("member", member);
//
//            // Redirect đến trang xem vé
//            int accountId = booking.getAccountId().getAccountId();
//            return "redirect:/viewTicket/" + accountId;
//        } else {
//            // Thanh toán thất bại
//            invoiceService.updateInvoiceStatus(invoiceId, -1);
//            model.addAttribute("transactionStatus", "Thất bại");
//            return "redirect:/booking/select-seat/" + getShowtimeIdByBookingId(extraData);
//        }
//    }
//
//    // TODO: Thêm phương thức này từ group3.MOVIETHEATER.controller.MomoController, cần kiểm tra tính đúng đắn
//    private int getShowtimeIdByBookingId(String bookingIdStr) {
//        int bookingId = Integer.parseInt(bookingIdStr);
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
//        return booking.getTickets().get(0)
//                .getShowtimeSeat()
//                .getShowtime()
//                .getShowtimeId();
//    }
//}