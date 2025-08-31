package movie_theater_gr4.project_gr4.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeDTO {
    private Long showtimeId;
    private Long roomId;
    private String roomName;
    private LocalDate showDate;
    private LocalDate startTime; // hoặc dùng LocalTime
    private Integer availableSeats;
    private LocalTime scheduleTime; // << thêm thời gian chiếu

}
