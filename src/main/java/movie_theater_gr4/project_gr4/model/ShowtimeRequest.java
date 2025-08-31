package movie_theater_gr4.project_gr4.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowtimeRequest {
    private LocalDate showDate;
    private LocalTime scheduleTime;
    private Long movieId;
    private Long roomId;
    private Long versionId;

}
