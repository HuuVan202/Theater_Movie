package movie_theater_gr4.project_gr4.bookingMember.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDetailDTO {

    private String movieName;
    private String versionName;
    private String ratingCode;
    private String ratingName;
    private String ratingDescription;
    private Long showDateId;
    private LocalDate showDate;
    private Long scheduleId;
    private LocalTime scheduleTime;
    private Long roomId;
    private String roomName;
    private Integer seatQuantity;
    private String screenType;
    private Integer status;
}
