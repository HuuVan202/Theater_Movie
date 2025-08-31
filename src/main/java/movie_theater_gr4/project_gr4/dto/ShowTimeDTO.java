package movie_theater_gr4.project_gr4.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowTimeDTO {
    private Long showtimeId;
    private MovieDTO movie;
    private ShowDateDTO showDate;
    private ScheduleDTO schedule;
    private CinemaRoomDTO room;
    private VersionDTO version;
//    private Integer availableSeats;
//    private String roomNumber;
    private List<ScheduleSeatDTO> scheduleSeats;
}
