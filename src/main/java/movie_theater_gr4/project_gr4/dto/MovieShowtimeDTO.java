package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieShowtimeDTO {
    private Long movieId;
    private String movieName;
    private List<VersionDTO> versions;
}
