package movie_theater_gr4.project_gr4.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    private Long ticketId;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Double price;

    private Long invoiceId;
    private Long scheduleSeatId;
}
