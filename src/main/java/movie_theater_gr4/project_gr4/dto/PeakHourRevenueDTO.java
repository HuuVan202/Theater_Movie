package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class PeakHourRevenueDTO {
    private String timeSlot; // Ví dụ: "08:00-11:00"
    private BigDecimal totalRevenue;
}
