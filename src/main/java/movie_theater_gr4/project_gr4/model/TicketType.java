package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ticket_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    private Long ticketTypeId;

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "price")
    private Double price;
}