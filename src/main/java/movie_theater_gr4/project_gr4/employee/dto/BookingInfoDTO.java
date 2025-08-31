package movie_theater_gr4.project_gr4.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInfoDTO {
    private String movieName;
    private String versionName;
    private Time scheduleTime;
    private String roomName;
    private Date showDate;
}
