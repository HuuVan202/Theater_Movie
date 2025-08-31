package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class PaymentMethodRevenueDTO {
    private String paymentMethod;
    private BigDecimal totalRevenue;
}
