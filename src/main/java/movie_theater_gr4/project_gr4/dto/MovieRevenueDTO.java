package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class MovieRevenueDTO {
    private String movieName;
    private BigDecimal totalRevenue;
    private Long showCount;
}
