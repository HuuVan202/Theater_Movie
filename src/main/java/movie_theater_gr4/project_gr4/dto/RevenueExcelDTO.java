package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueExcelDTO {
    private String label; // Ngày / Tuần / Tháng / Năm
    private BigDecimal totalRevenue;
}
