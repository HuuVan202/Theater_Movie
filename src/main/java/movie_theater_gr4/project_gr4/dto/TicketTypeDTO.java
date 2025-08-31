package movie_theater_gr4.project_gr4.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeDTO {
    private Long ticketTypeId;
    private String typeName;
    private Double price;
}
