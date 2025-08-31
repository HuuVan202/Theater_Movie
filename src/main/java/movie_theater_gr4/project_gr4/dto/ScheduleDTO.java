package movie_theater_gr4.project_gr4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long showtimeId; // ID của suất chiếu
    private Long scheduleId; // ID của khung giờ
    @JsonFormat(pattern = "HH:mm")
    private LocalTime scheduleTime; // Giờ chiếu
    private Integer availableSeats; // Số ghế trống
    private String roomNumber;
    private Integer seatQuantity;
}