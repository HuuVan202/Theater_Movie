package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueStatsDTO {
    private String month;
    private String quarter;
    private String year;
    private BigDecimal totalRevenue;

    public RevenueStatsDTO(String month, BigDecimal totalRevenue) {
        this.month = month;
        this.totalRevenue = totalRevenue;
    }


}
