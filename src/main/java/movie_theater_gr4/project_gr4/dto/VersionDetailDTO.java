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
public class VersionDetailDTO {
    private Long versionId;
    private String versionName;
    private String description;
    private List<ShowDateDTO> showDates;
}
