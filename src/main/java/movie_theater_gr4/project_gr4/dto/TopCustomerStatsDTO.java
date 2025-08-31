package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopCustomerStatsDTO {
    private String username;
    private BigDecimal ticketCount;
    private BigDecimal totalSpent; // Sửa từ Double thành BigDecimal để khớp với numeric(12,2)
}