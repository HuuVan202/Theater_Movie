package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
public class MovieTicketStatsDTO {
    private String movieName;
    private Long ticketCount; // Sửa từ BigDecimal thành BigInteger để khớp với COUNT
    private String month;    // Giữ String vì TO_CHAR trả về varchar (YYYY-MM)
    private String quarter;  // Giữ String vì CONCAT trả về varchar (YYYY-Qn)
    private Integer year; // Sửa từ BigDecimal thành Integer vì EXTRACT(YEAR) trả về số nguyên
    private BigDecimal totalRevenue;
    private Long totalShowtimes;
}
