package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class MovieExcelStatsDTO {
    private String movieName;
    private String genreNames;
    private Long ticketsSold;
    private Long showCount;
    private Long preBookedTickets;
    private BigDecimal totalRevenue;
}
