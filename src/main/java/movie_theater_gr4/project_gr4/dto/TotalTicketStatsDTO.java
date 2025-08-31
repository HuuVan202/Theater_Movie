package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotalTicketStatsDTO {
    private String month; // Thay LocalDate bằng String
    private String quarter; // Thay LocalDate bằng String
    private String year; // Thay LocalDate bằng String
    private Long totalTickets;
}