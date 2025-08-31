package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenreTicketStatsDTO {
    private String genreName;
    private String month; // Thay LocalDate bằng String
    private String quarter; // Thay LocalDate bằng String
    private String year; // Thay LocalDate bằng String
    private Long ticketCount;
}