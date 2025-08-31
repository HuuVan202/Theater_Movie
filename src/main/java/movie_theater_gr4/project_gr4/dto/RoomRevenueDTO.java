package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class RoomRevenueDTO {
    private String roomName;
    private String roomType;
    private BigDecimal totalRevenue;
}
