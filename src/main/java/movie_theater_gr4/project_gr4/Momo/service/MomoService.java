//package movie_theater_gr4.project_gr4.Momo.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class MomoService {
//
//    private final MomoConfig momoConfig;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public CreateMomoResponse createPaymentQRCode(long amount, String bookingId) {
//        try {
//            String requestId = UUID.randomUUID().toString();
//            String orderInfo = "Thanh toán vé xem phim";
//
//            // Dùng orderId kèm timestamp để tránh trùng
//            String momoOrderId = bookingId + "-" + System.currentTimeMillis();
//
//            // Đặt bookingId gốc vào extraData để sau truy ra được
//            String extraData = bookingId;
//
//            // ===== RAW DATA =====
//            String rawData = "accessKey=" + momoConfig.getAccessKey()
//                    + "&amount=" + amount
//                    + "&extraData=" + extraData
//                    + "&ipnUrl=" + momoConfig.getIpnUrl()
//                    + "&orderId=" + momoOrderId
//                    + "&orderInfo=" + orderInfo
//                    + "&partnerCode=" + momoConfig.getPartnerCode()
//                    + "&redirectUrl=" + momoConfig.getRedirectUrl()
//                    + "&requestId=" + requestId
//                    + "&requestType=" + momoConfig.getRequestType();
//
//            System.out.println("===== RAW DATA =====");
//            System.out.println(rawData);
//
//            // ===== SIGNATURE =====
//            String signature = signHmacSHA256(rawData, momoConfig.getSecretKey());
//            System.out.println("===== SIGNATURE =====");
//            System.out.println(signature);
//
//            // ===== Build request body =====
//            CreateMomoRequest request = CreateMomoRequest.builder()
//                    .partnerCode(momoConfig.getPartnerCode())
//                    .requestType(momoConfig.getRequestType())
//                    .ipnUrl(momoConfig.getIpnUrl())
//                    .orderId(momoOrderId)
//                    .amount(amount)
//                    .orderInfo(orderInfo)
//                    .requestId(requestId)
//                    .redirectUrl(momoConfig.getRedirectUrl())
//                    .extraData(extraData)
//                    .signature(signature)
//                    .build();
//
//            // ===== Gửi request POST đến MoMo =====
//            URL url = new URL("https://test-payment.momo.vn/v2/gateway/api/create");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//
//            String jsonRequest = objectMapper.writeValueAsString(request);
//            conn.getOutputStream().write(jsonRequest.getBytes(StandardCharsets.UTF_8));
//
//            Scanner scanner = new Scanner(conn.getInputStream());
//            StringBuilder responseBuilder = new StringBuilder();
//            while (scanner.hasNext()) {
//                responseBuilder.append(scanner.nextLine());
//            }
//
//            String jsonResponse = responseBuilder.toString();
//            System.out.println("===== MOMO RESPONSE =====");
//            System.out.println(jsonResponse);
//
//            return objectMapper.readValue(jsonResponse, CreateMomoResponse.class);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private String signHmacSHA256(String data, String key) throws Exception {
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(secretKeySpec);
//        byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//        StringBuilder sb = new StringBuilder();
//        for (byte b : hmacData) {
//            sb.append(String.format("%02x", b));
//        }
//        return sb.toString();
//    }
//}
