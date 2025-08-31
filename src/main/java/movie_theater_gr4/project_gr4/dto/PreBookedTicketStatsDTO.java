package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PreBookedTicketStatsDTO {
    private String movieName;
    private Long preBookedTickets;
    BigDecimal getTotalRevenue;
}