package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.model.Schedule;
import movie_theater_gr4.project_gr4.model.ShowDate;
import movie_theater_gr4.project_gr4.model.Showtime;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponseDTO {
    private MovieDTO movie;
    private List<ShowDate> showDates;
    private List<Showtime> showtimes;
    private List<Schedule> schedules;
}
