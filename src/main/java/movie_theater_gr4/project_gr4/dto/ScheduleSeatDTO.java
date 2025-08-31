package movie_theater_gr4.project_gr4.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSeatDTO {
    private Long scheduleSeatId;
    private BigDecimal seatPrice;
    private Integer status;
    private SeatDTO seat;
}