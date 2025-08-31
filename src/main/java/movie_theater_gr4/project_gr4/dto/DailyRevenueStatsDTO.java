package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class DailyRevenueStatsDTO {
    private String movieName;
    private BigDecimal totalRevenue;
    private Long totalTickets;
    private Long totalShowtimes;
}
