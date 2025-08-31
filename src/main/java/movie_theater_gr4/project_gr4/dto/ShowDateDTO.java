
package movie_theater_gr4.project_gr4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowDateDTO {
    private Long showDateId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate showDate;
    private List<ScheduleDTO> schedules;
}